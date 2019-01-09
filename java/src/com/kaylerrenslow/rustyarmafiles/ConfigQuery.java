package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

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
	public ResultRootNode match(@NotNull ConfigStream stream) {
		if (queryRootNode == null) {
			throw new IllegalStateException();
		}
		Stack<QueryNode> queryNodeStack = new Stack<>();
		Stack<ResultNode> resultNodeStack = new Stack<>();

		ResultRootNode resultRootNode = new ResultRootNode();
		resultNodeStack.push(resultRootNode);

		queryNodeStack.push(queryRootNode);

		while (stream.hasNext()) {
			ConfigStreamItem advancedItem = stream.advance();
			ResultNode resultNode = resultNodeStack.peek();
			QueryNode queryNode = queryNodeStack.peek();
			switch (advancedItem.getType()) {
				case Class: {
					if (queryNode == SKIP) {
						continue;
					}
					ConfigStreamItem.ClassItem classItem = (ConfigStreamItem.ClassItem) advancedItem;
					if (queryNode.containsClassName(classItem.getClassName())) {
						ClassResultNode classNode = new ClassResultNode(classItem.getClassName());
						resultNode.putChildClassNode(classNode);
					}
					break;
				}
				case SubClass: {
					if (queryNode == SKIP) {
						continue;
					}
					ConfigStreamItem.SubClassItem classItem = (ConfigStreamItem.SubClassItem) advancedItem;
					queryNode = queryNode.childQueryNode(classItem.getClassName());
					if (queryNode == null) {
						queryNodeStack.push(SKIP);
						break;
					}
					ClassResultNode child = new ClassResultNode(classItem.getClassName());
					resultNodeStack.push(child);
					queryNodeStack.push(queryNode);
					break;
				}
				case Assignment: {
					if (queryNode == SKIP) {
						continue;
					}
					ConfigStreamItem.AssignmentItem item = (ConfigStreamItem.AssignmentItem) advancedItem;
					if (queryNode.containsAssignmentKey(item.getKey())) {
						resultNode.putAssignment(item.getKey(), item.getValue());
					}
					break;
				}
				case EndSubClass: {
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

	private static class PatternParser {
		private final String query;

		public PatternParser(@NotNull String query) {
			this.query = query;
		}

		@NotNull
		public CompiledQuery parse() throws ParseException {
			int rbracketCount = 0;
			int lbracketCount = 0;
			boolean anticipateOperator = false;

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
					anticipateOperator = true;
					continue;
				}
				if (c == '{') {
					if (wordLength <= 0) {
						throw new ParseException("missing class name", i);
					}
					anticipateOperator = false;
					QueryNode node;
					String word = query.substring(wordStartIndex, wordLength + 1);
					if (word.equals("*")) {
						node = new AlwaysMatchClassNameQueryNode();
					} else {
						node = new QueryNode();
					}

					QueryNode peek = nodeStack.peek();
					peek.addChildClass(word, node);
					wordLength = 0;
					wordStartIndex = i + 1;
					lbracketCount++;
				} else if (c == ',') {
					if (wordLength <= 0) {
						throw new ParseException("missing assignment name", i);
					}
					anticipateOperator = false;
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
					anticipateOperator = false;
					rbracketCount++;
					wordLength = 0;
					wordStartIndex = i + 1;
					nodeStack.pop();
				} else {
					if (anticipateOperator) {
						throw new ParseException("Expected bracket or comma but got whitespace", i);
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
		private final Map<String, ConfigValue> assignments = new HashMap<>();
		private final Map<String, ClassResultNode> classes = new HashMap<>();

		@NotNull
		public ConfigValue getAssignmentValue(@NotNull String key) {
			ConfigValue val = assignments.get(key);
			if (val == null) {
				throw new IllegalArgumentException();
			}
			return val;
		}

		@NotNull
		public ConfigQuery.ClassResultNode getClassNode(@NotNull String key) {
			ClassResultNode node = classes.get(key);

			if (node == null) {
				throw new IllegalArgumentException();
			}

			return node;
		}

		void putAssignment(@NotNull String key, @NotNull ConfigValue value) {
			assignments.put(key, value);
		}

		void putChildClassNode(@NotNull ConfigQuery.ClassResultNode node) {
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
		public QueryNode getNode() {
			return node;
		}
	}

	private static class QueryNode {
		private final Map<String, QueryNode> children = new HashMap<>();
		private final Set<String> assignments = new HashSet<>();
		private boolean matchAllAssignments = false;

		private final Set<String> classNames = new HashSet<>();

		public QueryNode() {
		}

		@Nullable
		public QueryNode childQueryNode(@NotNull String key) {
			return children.get(key);
		}

		public boolean containsClassName(@NotNull String key) {
			return classNames.contains(key);
		}

		public void addChildClass(@NotNull String className, @NotNull QueryNode node) {
			children.put(className, node);
		}

		public void addAssignmentToMatch(@NotNull String key) {
			assignments.add(key);
		}

		void matchAllAssignments() {
			this.matchAllAssignments = true;
		}

		public boolean containsAssignmentKey(@NotNull String key) {
			return matchAllAssignments || assignments.contains(key);
		}
	}

	private static class AlwaysMatchClassNameQueryNode extends QueryNode {

		public AlwaysMatchClassNameQueryNode() {
			super();
		}

		@Override
		public boolean containsClassName(@NotNull String key) {
			return true;
		}

	}

	private static final QueryNode SKIP = new QueryNode();

	public static class ConfigQueryPatternBuilder {

		private final ConfigQueryPatternBuilder startBuilder;
		private final ConfigQueryPatternBuilder parentBuilder;
		private final QueryNode parent;

		private ConfigQueryPatternBuilder(@NotNull QueryNode parent) {
			this.parent = parent;
			this.parentBuilder = this;
			this.startBuilder = this;
		}

		@NotNull
		private ConfigQueryPatternBuilder(@NotNull QueryNode parent, @NotNull ConfigQueryPatternBuilder parentBuilder, @NotNull ConfigQueryPatternBuilder startBuilder) {
			this.parent = parent;
			this.parentBuilder = parentBuilder;
			this.startBuilder = startBuilder;
		}

		@NotNull
		public static ConfigQueryPatternBuilder start() {
			return new ConfigQueryPatternBuilder(new QueryNode());
		}

		@NotNull
		public ConfigQueryPatternBuilder matchClass(@NotNull String name) {
			QueryNode child = new QueryNode();
			ConfigQueryPatternBuilder builder = new ConfigQueryPatternBuilder(child, this, startBuilder);
			parent.children.put(name, child);
			return builder;
		}

		@NotNull
		public ConfigQueryPatternBuilder matchClasses(@NotNull String... names) {
			QueryNode child = new QueryNode();
			for (String name : names) {
				parent.children.put(name, child);
			}
			return this;
		}

		@NotNull
		public ConfigQueryPatternBuilder matchClassAndEnter(@NotNull String name) {
			QueryNode child = new QueryNode();
			parent.children.put(name, child);
			return new ConfigQueryPatternBuilder(child, this, startBuilder);
		}

		@NotNull
		public ConfigQueryPatternBuilder matchAssignment(@NotNull String name) {
			parent.assignments.add(name);
			return this;
		}

		@NotNull
		public ConfigQueryPatternBuilder matchAssignments(@NotNull String... names) {
			for (String name : names) {
				parent.assignments.add(name);
			}
			return this;
		}

		@NotNull
		public ConfigQueryPatternBuilder leaveClass(){
			return this.parentBuilder;
		}

		@NotNull
		public ConfigQuery.CompiledQuery compile(){
			return new CompiledQuery(startBuilder.parent);
		}

	}
}
