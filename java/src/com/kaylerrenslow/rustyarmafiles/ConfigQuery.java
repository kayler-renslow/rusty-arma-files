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
					if (queryNode.classNameMatches(classItem.getClassName())) {
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
					}
					ClassNode child = new ClassNode(classItem.getClassName());
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
		public ConfigQuery.QueryNode parse() throws ParseException {
			int rbracketCount = 0;
			int lbracketCount = 0;

			int wordStartIndex = 0;
			int wordLength = 0;

			Stack<QueryNode> nodeStack = new Stack<>();
			QueryNode rootNode = null;

			if (query.length() <= 0) {
				throw new ParseException("query is empty", 0);
			}

			for (int i = 0; i < query.length(); i++) {
				char c = query.charAt(i);
				if (Character.isWhitespace(c)) {
					throw new ParseException("whitespace not allowed", i);
				}
				if (c == '{') {
					if (wordLength <= 0) {
						throw new ParseException("missing class name", i);
					}
					QueryNode node = null;
					if (wordLength == 1) {
						if (query.charAt(wordStartIndex) == '*') {
							node = new AlwaysMatchQueryNode();
						} else {
							node = new QueryNode(query.charAt(wordStartIndex) + "");
						}
					} else {
						node = new QueryNode(query.substring(wordStartIndex, wordLength + 1));
					}

					if (nodeStack.isEmpty()) {
						nodeStack.add(node);
						rootNode = node;
					} else {
						QueryNode peek = nodeStack.peek();
						peek.addChild(node);
					}
					wordLength = 0;
					wordStartIndex = i + 1;
					lbracketCount++;
				} else if (c == ',') {
					if (wordLength <= 0) {
						throw new ParseException("missing assignment name", i);
					}
					String word = query.substring(wordStartIndex, wordLength + 1);
					if (nodeStack.isEmpty()) {
						/* example config where this happens
						field=1; //right here
						class MyClass; //another class could also come after
						*/
						QueryNode node = new AlwaysMatchQueryNode();
						nodeStack.push(node);
						rootNode = node;
					}

					QueryNode peek = nodeStack.peek();
					if (word.equals("*")) {
						peek.setMatchAllAssignments(true);
					} else {
						peek.addAssignmentToMatch(word);
					}

					wordLength = 0;
					wordStartIndex = i + 1;
				} else if (c == '}') {
					rbracketCount++;
					if (lbracketCount < rbracketCount) {
						throw new ParseException("unexpected }", i);
					}
					wordLength = 0;
					wordStartIndex = i + 1;
					nodeStack.pop();
				} else {
					wordLength++;
				}
			}
			if (lbracketCount > rbracketCount) {
				throw new ParseException("too few }", query.length());
			}
			if (rootNode == null) {
				rootNode = new AlwaysMatchQueryNode();
				if (query.equals("*")) {
					rootNode.setMatchAllAssignments(true);
				} else {
					rootNode.addAssignmentToMatch(query);
				}
			}
			return rootNode;
		}
	}

	private static class Node {
		private final Map<String, String> assignments = new HashMap<>();
		private final Map<String, ClassNode> classes = new HashMap<>();

		@NotNull
		public String getAssignmentValue(@NotNull String key) {
			String val = assignments.get(key);
			if (val == null) {
				throw new IllegalArgumentException();
			}
			return val;
		}

		@NotNull
		public ConfigQuery.ClassNode getClassNode(@NotNull String key) {
			ClassNode node = classes.get(key);

			if (node == null) {
				throw new IllegalArgumentException();
			}

			return node;
		}

		void putAssignment(@NotNull String key, @NotNull String value) {
			assignments.put(key, value);
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

	private static class QueryNode {
		private final Map<String, QueryNode> children = new HashMap<>();
		private final Set<String> assignmentsToMatch = new HashSet<>();
		private boolean matchAllAssignments = false;

		private final String classNameToMatch;

		public QueryNode(@NotNull String keyToMatch) {
			this.classNameToMatch = keyToMatch;
		}

		@Nullable
		public QueryNode childQueryNode(@NotNull String key) {
			return children.get(key);
		}

		public boolean classNameMatches(@NotNull String key) {
			return classNameToMatch.equals(key);
		}

		public void addChild(@NotNull QueryNode node) {
			children.put(node.classNameToMatch, node);
		}

		public void addAssignmentToMatch(@NotNull String key) {
			assignmentsToMatch.add(key);
		}

		void setMatchAllAssignments(boolean matchAllAssignments) {
			this.matchAllAssignments = matchAllAssignments;
		}

		public boolean containsAssignmentKey(@NotNull String key) {
			return matchAllAssignments || assignmentsToMatch.contains(key);
		}
	}

	private static class AlwaysMatchQueryNode extends QueryNode {

		public AlwaysMatchQueryNode() {
			super("");
		}

		@Override
		public boolean classNameMatches(@NotNull String key) {
			return true;
		}

	}

	private static final QueryNode SKIP = new QueryNode("");


}
