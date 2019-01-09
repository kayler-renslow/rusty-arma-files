package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

/**
 @author K
 @since 01/08/2019 */
public class ConfigQuery {

	public enum MatchType {
		ClassName, Field
	}

	private QueryNode queryRootNode;
	private final String query;

	public ConfigQuery(@NotNull String query) {

		this.query = query;
	}

	public void parseQuery() throws ParseException {
		if (queryRootNode != null) {
			return;
		}
		queryRootNode = new PatternParser(query).parse();
	}

	@NotNull
	public ResultRootNode match(@NotNull ConfigStream stream) {
		if (queryRootNode == null) {
			throw new IllegalStateException();
		}
		Stack<QueryNode> queryNodeStack = new Stack<>();
		Stack<Node> resultNodeStack = new Stack<>();

		ResultRootNode resultRootNode = new ResultRootNode();
		resultNodeStack.push(resultRootNode);

		queryNodeStack.push(queryRootNode);

		while (stream.hasNext()) {
			ConfigStreamItem advancedItem = stream.advance();
			Node resultNode = resultNodeStack.peek();
			QueryNode queryNode = queryNodeStack.peek();
			switch (advancedItem.getType()) {
				case Class: {
					if (queryNode == SKIP) {
						continue;
					}
					ConfigStreamItem.ClassItem classItem = (ConfigStreamItem.ClassItem) advancedItem;
					if (queryNode.keyToMatch.equals(classItem.getClassName())) {
						ClassNode classNode = new ClassNode(classItem.getClassName());
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
					} else {
						queryNodeStack.push(queryNode);
					}
					ClassNode child = new ClassNode(classItem.getClassName());
					resultNodeStack.push(child);
					break;
				}
				case Assignment: {
					if (queryNode == SKIP) {
						continue;
					}
					ConfigStreamItem.AssignmentItem item = (ConfigStreamItem.AssignmentItem) advancedItem;
					if (queryNode.keyToMatch.equals(item.getKey())) {
						resultNode.putChildAssignmentNode(new AssignmentNode(item.getKey(), item.getValue()));
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
		public ConfigQuery.QueryNode parse() throws ParseException {
			return null;
		}
	}

	private static class Node {
		private final Map<String, AssignmentNode> assignments = new HashMap<>();
		private final Map<String, ClassNode> classes = new HashMap<>();

		@NotNull
		public ConfigQuery.AssignmentNode getAssignmentNode(@NotNull String key) {
			AssignmentNode node = assignments.get(key);

			if (node == null) {
				throw new IllegalArgumentException();
			}
			return node;
		}

		@NotNull
		public ConfigQuery.ClassNode getClassNode(@NotNull String key) {
			ClassNode node = classes.get(key);

			if (node == null) {
				throw new IllegalArgumentException();
			}

			return node;
		}


		void putChildAssignmentNode(@NotNull ConfigQuery.AssignmentNode node) {
			assignments.put(node.key, node);
		}

		void putChildClassNode(@NotNull ConfigQuery.ClassNode node) {
			classes.put(node.className, node);
		}
	}

	public static class ClassNode extends Node {
		private final String className;

		public ClassNode(@NotNull String className) {
			this.className = className;
		}

		@NotNull
		public String getClassName() {
			return className;
		}
	}

	public static class ResultRootNode extends Node {

	}

	public static class AssignmentNode extends Node {
		private final String key;
		private String matchedValue;

		public AssignmentNode(@NotNull String key, @NotNull String matchedValue) {
			this.key = key;
			this.matchedValue = matchedValue;
		}

		@NotNull
		public String getKey() {
			return key;
		}

		@NotNull
		public String getMatchedValue() {
			return matchedValue;
		}
	}

	private static class QueryNode {
		private final Map<String, QueryNode> children = new HashMap<>();
		private final String keyToMatch;

		public QueryNode(@NotNull String keyToMatch) {
			this.keyToMatch = keyToMatch;
		}

		@Nullable
		public QueryNode childQueryNode(@NotNull String key) {
			return children.get(key);
		}
	}

	private static class AlwaysMatchQueryNode extends QueryNode {

		private final QueryNode child;

		public AlwaysMatchQueryNode(@NotNull QueryNode child) {
			super("");
			this.child = child;
		}

		@Override
		@NotNull
		public QueryNode childQueryNode(@NotNull String key) {
			return child;
		}
	}

	private static final QueryNode SKIP = new QueryNode("");


}
