package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

/**
 @author K
 @since 01/08/2019 */
public class ConfigQuery {

	private QueryNode queryRootNode;

	public ConfigQuery(@NotNull ConfigQuery.CompiledQuery query) {
		this.queryRootNode = query.getNode();
	}

	@NotNull
	public static CompiledQuery parseQuery(@NotNull String query) throws ParseException {
		return new PatternParser(query).parse();
	}

	@NotNull
	public static CompiledQuery parseArmaFormatQuery(@NotNull String query) throws ParseException {
		return new ArmaFormatQueryParser(query).parse();
	}

	@NotNull
	public ResultRootNode query(@NotNull ConfigStream stream) {
		if (queryRootNode == null) {
			throw new IllegalStateException();
		}
		Stack<QueryNode> queryNodeStack = new Stack<>();
		Stack<ResultNode> resultNodeStack = new Stack<>();

		ResultRootNode resultRootNode = new ResultRootNode();
		resultNodeStack.push(resultRootNode);

		queryNodeStack.push(queryRootNode);

		boolean skipClass = false;

		while (stream.hasNext()) {
			if (skipClass) {
				skipClass = false;
				stream.skipCurrentClass();
				continue;
			}
			ConfigStreamItem advancedItem = stream.next();
			ResultNode resultNode = resultNodeStack.peek();
			QueryNode queryNode = queryNodeStack.peek();
			switch (advancedItem.getType()) {
				// stream will always read assignments first,
				// then for each embedded class: read those assignments first, etc (recursion man)
				case Class: {
					ConfigStreamItem.ClassItem classItem = (ConfigStreamItem.ClassItem) advancedItem;
					if (queryNode.containsClassName(classItem.getClassName())) {
						ClassResultNode classNode = new ClassResultNode(classItem.getClassName());
						resultNode.putChildClassNode(classNode);
					}
					queryNode = queryNode.childQueryNode(classItem.getClassName());
					if (queryNode == null) {
						skipClass = true;
						break;
					}
					resultNodeStack.push(resultNode);
					queryNodeStack.push(queryNode);
					break;
				}
				case Assignment: {
					ConfigStreamItem.AssignmentItem item = (ConfigStreamItem.AssignmentItem) advancedItem;
					if (queryNode.containsAssignmentKey(item.getKey())) {
						resultNode.putAssignment(item.getKey(), item.getValue());
					}
					break;
				}
				case ClassSkipDone: {
					break;
				}
				case EndClass: {
					resultNodeStack.pop();
					queryNodeStack.pop();
					break;
				}
				default: {
					throw new IllegalStateException();
				}
			}
		}
		return resultRootNode;
	}

	private static class ArmaFormatQueryParser {
		private final String query;

		public ArmaFormatQueryParser(@NotNull String query) {
			this.query = query;
		}

		@NotNull
		public CompiledQuery parse() throws ParseException {
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
						oldNode.addAssignmentToMatch(word);
						expectWord = true;
					} else {
						expectOperator = true;
						expectWord = false;
					}
					continue;
				}
				if (!Character.isAlphabetic(c)) {
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
				currentNode.addAssignmentToMatch(word);
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
					}
					QueryNode node = new QueryNode();
					peek.addChildClass(word, node);
					nodeStack.push(node);

					wordLength = 0;
					wordStartIndex = i + 1;
					lbracketCount++;
				} else if (c == ',') {
					if (wordLength <= 0) {
						throw new ParseException("missing assignment name", i);
					}
					expectOperator = false;
					QueryNode peek = nodeStack.peek();
					String word = query.substring(wordStartIndex, wordLength + 1);
					if (word.equals("*")) {
						peek.matchAllAssignments();
					} else {
						peek.addAssignmentToMatch(word);
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
					if (!Character.isAlphabetic(c)) {
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
					rootNode.matchAllAssignments();
				} else {
					//don't add whole query because there may be whitespace
					rootNode.addAssignmentToMatch(query.substring(wordStartIndex, wordLength + 1));
				}
			}
			return new CompiledQuery(rootNode);
		}
	}

	private static class ResultNode {
		private Map<String, ConfigFieldValue> assignments;
		private Map<String, ClassResultNode> classes;

		@Nullable
		public ConfigFieldValue getAssignmentValue(@NotNull String key) {
			return assignments == null ? null :  assignments.get(key);
		}

		@Nullable
		public ConfigQuery.ClassResultNode getClassNode(@NotNull String key) {
			return classes == null ? null : classes.get(key);
		}

		@Nullable
		public Iterator<Map.Entry<String, ConfigFieldValue>> iterateAssignments(){
			return assignments == null ? null : assignments.entrySet().iterator();
		}

		@Nullable
		public Iterator<Map.Entry<String, ClassResultNode>> iterateClasses(){
			return classes == null ? null : classes.entrySet().iterator();
		}

		void putAssignment(@NotNull String key, @NotNull ConfigFieldValue value) {
			if (assignments == null) {
				//lazy initialization to save memory
				assignments = new HashMap<>();
			}
			assignments.put(key, value);
		}

		void putChildClassNode(@NotNull ConfigQuery.ClassResultNode node) {
			if (classes == null) {
				//lazy initialization to save memory
				classes = new HashMap<>();
			}
			classes.put(node.className, node);
		}
	}

	public static class ClassResultNode extends ResultNode {
		private final String className;

		public ClassResultNode(@NotNull String className) {
			this.className = className;
		}

		@NotNull
		public String getClassName() {
			return className;
		}
	}


	public static class ResultRootNode extends ResultNode {

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
		private final Set<String> assignments = new HashSet<>();
		private boolean matchAllAssignments = false;
		private boolean matchAllClasses = false;

		private final Set<String> classNames = new HashSet<>();

		public QueryNode() {
		}

		@Nullable
		public QueryNode childQueryNode(@NotNull String key) {
			return children.get(key);
		}

		public boolean containsClassName(@NotNull String key) {
			return matchAllClasses || classNames.contains(key);
		}

		public void addChildClass(@NotNull String className, @NotNull QueryNode node) {
			children.put(className, node);
			classNames.add(className);
		}

		public void addAssignmentToMatch(@NotNull String key) {
			assignments.add(key);
		}

		public void matchAllAssignments() {
			this.matchAllAssignments = true;
		}

		public void matchAllClasses() {
			this.matchAllClasses = true;
		}

		public boolean containsAssignmentKey(@NotNull String key) {
			return matchAllAssignments || assignments.contains(key);
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
			parent.assignments.add(name);
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAssignments(@NotNull String... names) {
			for (String name : names) {
				parent.assignments.add(name);
			}
			return this;
		}

		@NotNull
		public ConfigQuery.ConfigQueryBuilder matchAllAssignments() {
			parent.matchAllAssignments();
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
