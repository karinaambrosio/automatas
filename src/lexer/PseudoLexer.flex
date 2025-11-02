package lexer;

import java_cup.runtime.Symbol;
import java.util.ArrayList;
import java.util.List;

%public
%class PseudoLexer
%implements java_cup.runtime.Scanner
%unicode
%cup
%line
%column


%{
  public int yyline = 1;
  public int yycolumn = 0;
  public java.util.List<String> lexErrors = new ArrayList<>();
%}

/* macros */
WSP = [ \t\f]+
LETTER = [A-Za-z]
DIGIT = [0-9]
ID = {LETTER}({LETTER}|{DIGIT}|_)*
NUM = {DIGIT}+

%%

/* Newline (no usamos macro para evitar problemas de encoding) */
(\r\n|[\r\n])                  { yyline++; yycolumn = 0; }

/* whitespace */
{WSP}                         { /* ignore whitespace */ }

/* comments */
"<!--"([^>]|\r|\n)*"-->"      { /* comment, ignore */ }

/* Tags (los patrones más largos antes que los más cortos) */
"<funcion>"                   { return new Symbol(sym.FUNC_OPEN, yyline, yycolumn, "<funcion>"); }
"</funcion>"                  { return new Symbol(sym.FUNC_CLOSE, yyline, yycolumn, "</funcion>"); }

"<parametros>"                { return new Symbol(sym.PARAM_OPEN, yyline, yycolumn, "<parametros>"); }
"</parametros>"               { return new Symbol(sym.PARAM_CLOSE, yyline, yycolumn, "</parametros>"); }

"<codigo>"                    { return new Symbol(sym.COD_OPEN, yyline, yycolumn, "<codigo>"); }
"</codigo>"                   { return new Symbol(sym.COD_CLOSE, yyline, yycolumn, "</codigo>"); }

"<if>"                        { return new Symbol(sym.IF_OPEN, yyline, yycolumn, "<if>"); }
"</if>"                       { return new Symbol(sym.IF_CLOSE, yyline, yycolumn, "</if>"); }

"<condicion>"                 { return new Symbol(sym.COND_OPEN, yyline, yycolumn, "<condicion>"); }
"</condicion>"                { return new Symbol(sym.COND_CLOSE, yyline, yycolumn, "</condicion>"); }

"<do>"                        { return new Symbol(sym.DO_OPEN, yyline, yycolumn, "<do>"); }
"</do>"                       { return new Symbol(sym.DO_CLOSE, yyline, yycolumn, "</do>"); }

/* Operators and punctuation (pongan antes de TEXT) */
"&&"                          { return new Symbol(sym.ANDAND, yyline, yycolumn, "&&"); }
"||"                          { return new Symbol(sym.OROR, yyline, yycolumn, "||"); }
">="                          { return new Symbol(sym.GTE, yyline, yycolumn, ">="); } /* opcional */
"<="                          { return new Symbol(sym.LTE, yyline, yycolumn, "<="); } /* opcional */
">"                           { return new Symbol(sym.GT, yyline, yycolumn, ">"); }
"<"                           { return new Symbol(sym.LT, yyline, yycolumn, "<"); }
"="                           { return new Symbol(sym.ASSIGN, yyline, yycolumn, "="); }
";"                           { return new Symbol(sym.SEMI, yyline, yycolumn, ";"); }
","                           { return new Symbol(sym.COMMA, yyline, yycolumn, ","); }
"("                           { return new Symbol(sym.LPAREN, yyline, yycolumn, "("); }
")"                           { return new Symbol(sym.RPAREN, yyline, yycolumn, ")"); }
"+"                           { return new Symbol(sym.PLUS, yyline, yycolumn, "+"); }
"-"                           { return new Symbol(sym.MINUS, yyline, yycolumn, "-"); }
"*"                           { return new Symbol(sym.TIMES, yyline, yycolumn, "*"); }
"/"                           { return new Symbol(sym.DIV, yyline, yycolumn, "/"); }

/* Identifiers and numbers */
{ID}                          { return new Symbol(sym.ID, yyline, yycolumn, yytext()); }
{NUM}                         { return new Symbol(sym.NUM, yyline, yycolumn, yytext()); }

/* Text between tags (any run of characters not containing '<' or newline) */
[^<\r\n]+                     {
                                String t = yytext().trim();
                                if (t.length() == 0) { /* ignore */ }
                                else return new Symbol(sym.TEXT, yyline, yycolumn, t);
                              }

/* Unknown tag starting with '<' (catch-all for tags not matched above) */
"<"[^>]*">"                   {
                                lexErrors.add("Error léxico en línea " + yyline + ": etiqueta desconocida '" + yytext() + "'");
                                return new Symbol(sym.TEXT, yyline, yycolumn, yytext());
                              }

/* any other single char (error) */
.                             {
                                lexErrors.add("Caracter no reconocido en línea " + yyline + ": '" + yytext() + "'");
                                return new Symbol(sym.TEXT, yyline, yycolumn, yytext());
                              }

/* EOF */
<<EOF>>                       { return new Symbol(sym.EOF, yyline, yycolumn, "EOF"); }
