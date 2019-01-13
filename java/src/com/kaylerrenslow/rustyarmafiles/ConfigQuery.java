package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

/**
 This class is used for filtering through an existing {@link ConfigStream} and returning only {@link ConfigStreamItem}'s
 that match a query.

 @author K
 @since 01/08/2019 */
public class ConfigQuery {

	private final QueryNode queryRootNode;

	public ConfigQuery(@NotNull ConfigQuery.CompiledQuery query) {
		this.queryRootNode = query.getNode();
	}

	@NotNull
	public static CompiledQuery parseQuery(@NotNull String query) throws ParseException {
		return new PatternParser(query).parse();
	}

	/**
	 Parses an Arma format query. Eg. ConfigFile>>"CfgVehicles">>"AVehicle".
	 The parser doesn't need quotes around each item. The parser also has the option for incomplete names.
	 Use ? to denote that the name is incomplete and should match the first class/field with the name up to the ?.
	 Example:
	 Query: Thing>>Thing? will match Thing>>Thing, Thing>>Thing2, Thing>>ThingThing, etc

	 @param query           the query text
	 @param allowIncomplete true if the query can match incomplete/partial names
	 @return the compiled query
	 @throws ParseException when the query couldn't be compiled
	 */
	@NotNull
	public static CompiledQuery parseArmaFormatQuery(@NotNull String query, boolean allowIncomplete) throws ParseException {
		return new ArmaFormatQueryParser(query).parse(allowIncomplete);
	}

	@NotNull
	public ConfigStream query(@NotNull ConfigStream stream) {
		if (queryRootNode == null) {
			throw new IllegalStateException();
		}
		return new QueryResultConfigStream(stream, queryRootNode);
	}

	private static final class QueryResultConfigStream implements ConfigStream {

		private final ConfigStream src;
		private final Stack<QueryNode> queryNodeStack = new Stack<>();
		private boolean querySkipClass = false;

		public QueryResultConfigStream(@NotNull ConfigStream src, @NotNull QueryNode root) {
			this.src = src;
			queryNodeStack.push(root);
		}

		@Override
		@NotNull
		public ConfigStreamItem next() throws ConfigStreamException {
			while (src.hasNext()) { //loop until a match
				ConfigStreamItem nextItem = src.next();
				QueryNode queryNode = queryNodeStack.peek();

				switch (nextItem.getType()) {
					// Stream will always read assignments first,
					// then for each embedded class: read those assignments first, etc (recursion man).
					case Class: {
						ConfigStreamItem.ClassItem classItem = (ConfigStreamItem.ClassItem) nextItem;
						// No need to check if the class is the root class because it won't be returned as a ClassItem
						// and instead will immediately just stream the fields.
						final boolean match = queryNode.containsClassName(classItem.getClassName());
						if (match) {
							QueryNode nextNode = queryNode.childQueryNode(classItem.getClassName());
							if (queryNode.isMatchAllClasses()) {
								if (nextNode == null) {
									nextNode = queryNode.childQueryNode("*");
								}
							} else {
								throw new IllegalStateException(); //how??
							}
							if (nextNode != null) {
								queryNodeStack.push(nextNode);
								return nextItem;
							}
						}
						querySkipClass = true;
						src.skipCurrentClass();
						break;
					}
					case Field: {
						ConfigStreamItem.FieldItem item = (ConfigStreamItem.FieldItem) nextItem;
						if (queryNode.containsFieldKey(item.getKey())) {
							return nextItem;
						}
						break;
					}
					case EndClass: {
						if (querySkipClass) {
							querySkipClass = false;
							break;
						}
						queryNodeStack.pop();
						return nextItem;
					}
					case EndStream: {
						return nextItem;
					}
					default: {
						throw new IllegalStateException();
					}
				}
			}
			return ConfigStreamItem.EndStreamItem.INSTANCE;
		}

		@Override
		public boolean hasNext() {
			return src.hasNext();
		}

		@Override
		public void skipCurrentClass() {
			src.skipCurrentClass();
		}
	}

	private static class ArmaFormatQueryParser {
		private final String query;

		public ArmaFormatQueryParser(@NotNull String query) {
			this.query = query;
		}

		@NotNull
		public CompiledQuery parse(boolean allowIncomplete) throws ParseException {
			int wordLength = 0;
			int wordStartIndex = 0;
			boolean expectOperator = true;
			int bracketCount = 0;
			boolean expectWord = true;

			QueryNode currentNode = new QueryNode();
			QueryNode start = currentNode;

			for (int i = 0; i < query.length(); i++) {
				char c = query.charAt(i);
				if (Character.isWhitespace(c)) {
					if (wordLength > 0) {
						expectOperator = true;
					} else {
						wordStartIndex++;
					}
					continue;
				}
				if (c == '>') {
					if (expectWord) {
						throw new ParseException("expected a word, got >", i);
					}
					bracketCount++;
					if (bracketCount > 2) {
						throw new ParseException("expected word, got >", i);
					}
					if (bracketCount == 2) {
						expectOperator = false;
						String word = query.substring(wordStartIndex, wordLength + 1);

						//match either class or assignment since the >> in arma 3 matches entry name and doesn't care about type
						QueryNode oldNode = currentNode;
						currentNode = new QueryNode();
						oldNode.addChildClass(word, currentNode);
						oldNode.addFieldToMatch(word);
						if (allowIncomplete && word.charAt(word.length() - 1) == '?') {
							oldNode.matchIncompleteClassNames();
							oldNode.matchIncompleteFieldKeys();
						}
						expectWord = true;
					} else {
						expectOperator = true;
						expectWord = false;
					}
					continue;
				}
				if (!Character.isAlphabetic(c) && !(allowIncomplete && c == '?')) {
					throw new ParseException("Unexpected token: " + c, i);
				}
				wordLength++;
				expectWord = false;
			}
			if (expectOperator) {
				throw new ParseException("Unexpected >", query.length());
			}
			if (expectWord) {
				throw new ParseException("Expected a word", query.length());
			}
			if (wordLength > 0) {
				String word = query.substring(wordStartIndex, wordLength + 1);

				//match either class or assignment since the >> in arma 3 matches entry name and doesn't care about type
				currentNode.addChildClass(word, currentNode);
				currentNode.addFieldToMatch(word);
			}
			return new CompiledQuery(start);
		}
	}

	private static class PatternParser {
		private final String query;

		public PatternParser(@NotNull String query) {
			this.query = query;
		}

		@NotNull
		public CompiledQuery parse() throws ParseException {
			int rbracketCount = 0;
			int lbracketCount = 0;
			boolean expectOperator = false;

			int wordStartIndex = 0;
			int wordLength = 0;

			Stack<QueryNode> nodeStack = new Stack<>();
			nodeStack.push(new QueryNode());

			if (query.length() <= 0) {
				throw new ParseException("query is empty", 0);
			}

			for (int i = 0; i < query.length(); i++) {
				char c = query.charAt(i);
				if (Character.isWhitespace(c)) {
					if (wordLength > 0) {
						expectOperator = true;
					} else {
						wordStartIndex++;
					}
					continue;
				}
				if (c == '{') {
					if (wordLength <= 0) {
						throw new ParseException("missing class name", i);
					}
					expectOperator = false;
					String word = query.substring(wordStartIndex, wordLength + 1);
					QueryNode peek = nodeStack.peek();
					if (word.equals("*")) {
						peek.matchAllClasses();
					} else if (word.charAt(word.length() - 1) == '?') {
						peek.matchIncompleteClassNames();
					}
					QueryNode node = new QueryNode();
					peek.addChildClass(word, node);
					nodeStack.push(node);

					wordLength = 0;
					wordStartIndex = i + 1;
					lbracketCount++;
				} else if (c == ';') {
					if (wordLength <= 0) {
						throw new ParseException("missing assignment name", i);
					}
					expectOperator = false;
					QueryNode peek = nodeStack.peek();
					String word = query.substring(wordStartIndex, wordLength + 1);
					if (word.equals("*")) {
						peek.matchAllFields();
					} else {
						if (word.charAt(word.length() - 1) == '?') {
							peek.matchIncompleteFieldKeys();
						}
						peek.addFieldToMatch(word);
					}

					wordLength = 0;
					wordStartIndex = i + 1;
				} else if (c == '}') {
					if (lbracketCount < rbracketCount) {
						throw new ParseException("unexpected }", i);
					}
					expectOperator = false;
					rbracketCount++;
					wordLength = 0;
					wordStartIndex = i + 1;
					nodeStack.pop();
				} else {
					if (expectOperator) {
						throw new ParseException("Expected bracket or comma but got whitespace", i);
					}
					if (!Character.isAlphabetic(c) && c != '?') {
						throw new ParseException("Unexpected token: " + c, i);
					}
					wordLength++;
				}
			}
			if (lbracketCount > rbracketCount) {
				throw new ParseException("too few }", query.length());
			}
			QueryNode rootNode = nodeStack.peek();
			if (wordLength > 0) {
				if (query.equals("*")) {
					rootNode.matchAllFields();
				} else {
					//don't add whole query because there may be whitespace
					rootNode.addFieldToMatch(query.substring(wordStartIndex, wordLength + 1));
				}
			}
			return new CompiledQuery(rootNode);
		}
	}

	public static class CompiledQuery {
		private final QueryNode node;

		private CompiledQuery(@NotNull QueryNode node) {
			this.node = node;
		}

		@NotNull
		QueryNode getNode() {
			return node;
		}
	}

	private static class QueryNode {
		private final Map<String, QueryNode> children = new HashMap<>();
		private final Set<String> fields = new HashSet<>();
		private boolean matchAllFields = false;
		private boolean matchAllClasses = false;
		private boolean matchIncompleteClassNames = false;
		private boolean matchIncompleteFieldKeys = false;

		@Nullable
		public QueryNode childQueryNode(@NotNull String className) {
			if (matchIncompleteClassNames) {
				Map.Entry<String, QueryNode> match = matchIncompleteClassName(className);
				return match == null ? null : match.getValue();
			}
			return children.get(className);
		}

		public boolean containsClassName(@NotNull String key) {
			return matchAllClasses || children.containsKey(key);
		}

		public void addChildClass(@NotNull String className, @NotNull QueryNode node) {
			children.put(className, node);
		}

		public void addFieldToMatch(@NotNull String key) {
			fields.add(key);
		}

		public void matchAllFields() {
			this.matchAllFields = true;
		}

		public void matchAllClasses() {
			this.matchAllClasses = true;
		}

		public boolean isMatchAllClasses() {
			return matchAllClasses;
		}

		public void matchIncompleteClassNames() {
			this.matchIncompleteClassNames = true;
		}

		public void matchIncompleteFieldKeys() {
			this.matchIncompleteFieldKeys = true;
		}

		public boolean containsFieldKey(@NotNull String key) {
			if (matchAllFields) {
				return true;
			}
			if (matchIncompleteFieldKeys) {
				for (String fieldKey : fields) {
					if (key.contains(fieldKey)) {
						return true;
					}
				}
				return false;
			}
			return fields.contains(key);
		}

		@Nullable
		private Map.Entry<String, QueryNode> matchIncompleteClassName(@NotNull String className) {
			for (Map.Entry<String, QueryNode> incompleteClassNameEntry : children.entrySet()) {
				if (className.contains(incompleteClassNameEntry.getKey())) {
					return incompleteClassNameEntry;
				}
			}
			return null;
		}
	}

	public static class ConfigQueryBuilder {

		private final ConfigQueryBuilder startBuilder;
		private final ConfigQueryBuilder parentBuilder;
		private final QueryNode parent;

		private ConfigQueryBuilder(@NotNull QueryNode parent) {
			this.parent = parent;
			this.parentBuilder = this;
			this.startBuilder = this;
		}

		@NotNull
		private ConfigQueryBuilder(@NotNull QueryNode parent, @NotNull ConfigQuery.ConfigQueryBuilder parentBuilder, @NotNull ConfigQuery.ConfigQueryBuilder startBuilder) {
			this.parent = parent;
			this.parentBuilder = parentBuilder;
			this.startBuilder = startBuilder;
		}

		@NotNull
		public static ConfigQuery.ConfigQueryBuilder start() {
			return new ConfigQueryBuilder(new QueryNode());
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAllClasses() {
			parent.matchAllClasses();
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAllClassesAndEnter() {
			parent.matchAllClasses();
			return new ConfigQueryBuilder(new QueryNode(), this, startBuilder);
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchClass(@NotNull String name) {
			QueryNode child = new QueryNode();
			parent.children.put(name, child);
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchClasses(@NotNull String... names) {
			QueryNode child = new QueryNode();
			for (String name : names) {
				parent.children.put(name, child);
			}
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchClassesAndForEach(@NotNull Consumer<ConfigQueryBuilder> visitor, @NotNull String... names) {
			for (String name : names) {
				ConfigQueryBuilder builder = new ConfigQueryBuilder(new QueryNode(), this, startBuilder);
				parent.children.put(name, builder.parent);
				visitor.accept(builder);
			}
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchClassAndEnter(@NotNull String name) {
			QueryNode child = new QueryNode();
			parent.children.put(name, child);
			return new ConfigQueryBuilder(child, this, startBuilder);
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAssignment(@NotNull String name) {
			parent.fields.add(name);
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAssignments(@NotNull String... names) {
			for (String name : names) {
				parent.fields.add(name);
			}
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAllAssignments() {
			parent.matchAllFields();
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder leaveClass() {
			return this.parentBuilder;
		}

		@NotNull
		public ConfigQuery.CompiledQuery compile() {
			return new CompiledQuery(startBuilder.parent);
		}


	}

}
