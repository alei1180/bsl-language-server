package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.utils.Ranges;
import com.github._1c_syntax.bsl.languageserver.utils.RelatedInformation;
import com.github._1c_syntax.bsl.languageserver.utils.Trees;
import com.github._1c_syntax.bsl.parser.BSLParserRuleContext;
import com.github._1c_syntax.bsl.parser.SDBLParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DiagnosticMetadata(
  type = DiagnosticType.ERROR,
  severity = DiagnosticSeverity.CRITICAL,
  minutesToFix = 2,
  tags = {
    DiagnosticTag.SQL,
    DiagnosticTag.SUSPICIOUS,
    DiagnosticTag.UNPREDICTABLE
  }

)
public class FieldsFromConnectionsWithoutIsNullDiagnostic extends AbstractSDBLVisitorDiagnostic {

  private static final List<Integer> RULE_QUERIES = Arrays.asList(SDBLParser.RULE_query, SDBLParser.RULE_temporaryTableMainQuery);

  private static final Integer SELECT_STATEMENTS_ROOT = SDBLParser.RULE_selectedField;
  private static final List<Integer> SELECT_STATEMENTS = Arrays.asList(SELECT_STATEMENTS_ROOT, SDBLParser.RULE_selectStatement);
  private static final Integer WHERE_STATEMENTS_ROOT = SDBLParser.RULE_where;
  private static final List<Integer> WHERE_STATEMENTS = Arrays.asList(WHERE_STATEMENTS_ROOT, SDBLParser.RULE_whereStatement);
  private static final Integer JOIN_STATEMENTS_ROOT = SDBLParser.RULE_joinPart;
  private static final List<Integer> JOIN_STATEMENTS = Arrays.asList(JOIN_STATEMENTS_ROOT, SDBLParser.RULE_joinStatement);

  private final List<BSLParserRuleContext> nodesForIssues = new ArrayList<>();

  @Override
  public ParseTree visitJoinPart(SDBLParser.JoinPartContext joinPartCtx) {

    try {
      joinedTables(joinPartCtx)
        .forEach(tableName -> checkQuery(tableName, joinPartCtx));

      if (!nodesForIssues.isEmpty()){
        diagnosticStorage.addDiagnostic(joinPartCtx, getRelatedInformation(joinPartCtx));
      }

    } catch (Exception e){
      nodesForIssues.clear();
      throw e;
    }
    nodesForIssues.clear();

    return super.visitJoinPart(joinPartCtx);
  }

  private Stream<String> joinedTables(SDBLParser.JoinPartContext joinPartCtx) {
    return Optional.of(joinPartCtx)
      .stream().flatMap(joinPartContext ->  joinedDataSourceContext(joinPartContext).stream())
      .filter(Objects::nonNull)
      .map(SDBLParser.DataSourceContext::alias)
      .map(SDBLParser.AliasContext::identifier)
      .map(BSLParserRuleContext::getText);
  }

  private List<SDBLParser.DataSourceContext> joinedDataSourceContext(SDBLParser.JoinPartContext joinPartContext) {
    if (joinPartContext.LEFT() != null){
      return Collections.singletonList(joinPartContext.dataSource());
    }
    else if(joinPartContext.RIGHT() != null){
      return Collections.singletonList(((SDBLParser.DataSourceContext) joinPartContext.getParent()));
    }
    else if(joinPartContext.FULL() != null){
      return Arrays.asList(((SDBLParser.DataSourceContext) joinPartContext.getParent()),
        joinPartContext.dataSource());
    }
    return Collections.emptyList();
  }

  private void checkQuery(String joinedTableName, SDBLParser.JoinPartContext joinPartCtx) {
    Optional.ofNullable(Trees.getRootParent(joinPartCtx, RULE_QUERIES))
      .ifPresent(queryCtx -> {
        checkSelect(joinedTableName, queryCtx);
        checkWhere(joinedTableName, queryCtx);
      });

//    TODO проверить и RULE_query и RULE_temporaryTableMainQuery

//    TODO исключить замечание, если в условии ГДЕ есть проверка на ЕСТЬ NULL (или НЕ ЕСТЬ NULL ?? )

    checkAllJoins(joinedTableName, joinPartCtx);
// TODO нужно проверять любые выражения - из СГРУППИРОВАТЬ, ИМЕЮЩИЕ и т.п.
  }

  private void checkSelect(String tableName, BSLParserRuleContext query) {
    Trees.getFirstChild(query, SDBLParser.RULE_selectedFields)
      .ifPresent(ctx -> checkStatements(tableName, ctx, SDBLParser.RULE_selectStatement, SELECT_STATEMENTS, SELECT_STATEMENTS_ROOT));
  }

  private void checkStatements(
    String tableName, BSLParserRuleContext expression, Integer parentStatementIndex,
    List<Integer> statements, Integer statementsRoot) {

    final var columnContextStream = Optional.of(expression)
      .stream().flatMap(ctx -> Trees.findAllRuleNodes(ctx, parentStatementIndex).stream())
      .flatMap(parseTree -> Trees.getFirstChild(parseTree, SDBLParser.RULE_statement).stream())
      .filter(statementContext -> statementContext.getRuleIndex() != SDBLParser.ISNULL)
      .flatMap(column -> Trees.getFirstChild(column, SDBLParser.RULE_column).stream())
      .filter(ctx -> ctx instanceof SDBLParser.ColumnContext)
      .map(ctx -> (SDBLParser.ColumnContext)ctx);

    checkColumn(tableName, columnContextStream, statements, statementsRoot);
  }

  private void checkColumn(String tableName, Stream<SDBLParser.ColumnContext> columnContextStream,
                           List<Integer> statements, Integer statementsRoot) {
    columnContextStream.filter(Objects::nonNull)
      .filter(columnContext -> columnContext.tableName.getText().equalsIgnoreCase(tableName))
      .filter(columnContext -> dontInnerIsNull(columnContext, statements, statementsRoot))
      .forEach(nodesForIssues::add);
  }

  private boolean dontInnerIsNull(BSLParserRuleContext ctx, List<Integer> statements, Integer rootParentIndex) {
    var selectStatement = Trees.getRootParent(ctx, statements);
    if (selectStatement == null || selectStatement.getRuleIndex() == rootParentIndex || selectStatement.getChildCount() == 0){
      return true;
    }
    final var child = selectStatement.getChild(0);
    if (child instanceof TerminalNode && ((TerminalNode)child).getSymbol().getType() == SDBLParser.ISNULL){
      return false;
    }
    return dontInnerIsNull((BSLParserRuleContext)selectStatement.getParent(), statements, rootParentIndex);
  }

  private void checkWhere(String tableName, BSLParserRuleContext query) {
    Trees.getFirstChild(query, SDBLParser.RULE_where)
      .filter(bslParserRuleContext -> bslParserRuleContext.getChildCount() > 0)
      .map(ctx -> (SDBLParser.WhereContext) ctx)
      .map(SDBLParser.WhereContext::whereExpression)
      .ifPresent(exprCtx -> checkStatements(tableName, exprCtx, SDBLParser.RULE_whereStatement, WHERE_STATEMENTS, WHERE_STATEMENTS_ROOT));

  }

  private void checkAllJoins(String tableName, SDBLParser.JoinPartContext currentJoinPart) {
    Optional.ofNullable(Trees.getRootParent(currentJoinPart, SDBLParser.RULE_dataSource))
      .filter(ctx -> ctx instanceof SDBLParser.DataSourceContext)
      .stream().flatMap(ctx -> ((SDBLParser.DataSourceContext)ctx).joinPart().stream())
      .filter(joinPartContext -> joinPartContext != currentJoinPart)
      .map(SDBLParser.JoinPartContext::joinExpression)
      .forEach(joinExpressionContext -> checkStatements(tableName, joinExpressionContext, SDBLParser.RULE_joinStatement, JOIN_STATEMENTS, JOIN_STATEMENTS_ROOT));
  }

  private List<DiagnosticRelatedInformation> getRelatedInformation(SDBLParser.JoinPartContext self) {
    return nodesForIssues.stream()
      .filter(ctx -> !ctx.equals(self))
      .map(context -> RelatedInformation.create(
        documentContext.getUri(),
        Ranges.create(context),
        "+1"
      )).collect(Collectors.toList());
  }
}
