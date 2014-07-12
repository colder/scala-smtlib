package smtlib
package parser

import lexer.Tokens
import Tokens.Token
import lexer.Lexer

import Terms._
import Commands._

import common._


class Parser(lexer: Lexer) {

  import Parser._

  private var _currentToken: Token = null
  /* lookAhead token is Some(null) if we reached eof */
  private var _lookAhead: Option[Token] = None

  //return a next token or null if EOF
  private def nextToken: Token = {
    _lookAhead match {
      case Some(t) => {
        _lookAhead = None
        _currentToken = t
        t
      }
      case None => {
        _currentToken = lexer.nextToken
        _lookAhead = None
        _currentToken
      }
    }
  }

  /*
   * return the look ahead token, or null if EOF
   * Note: do not throw an exception as it is okay to lookahead into EOF
   */
  private def peekToken: Token = {
    _lookAhead match {
      case Some(t) => t
      case None => {
        _lookAhead = Some(lexer.nextToken)
        _lookAhead.get
      }
    }
  }

  /*
   * Make sure the next token corresponds to t and read
   */
  private def eat(expected: Token): Unit = {
    val token = nextToken
    check(token, expected)
  }

  private def check(current: Token, expected: Token): Unit = {
    if(current != expected) {
      sys.error("unexpected token")
    }
  }


  def parse: Script = {
    ???
  }

  def parseCommand: Command = {
    val head = nextToken
    check(head, Tokens.OParen())

    val cmd = nextToken match {
      case Tokens.SetLogic() => {
        val logicSymbol: SSymbol = parseSymbol
        val logic = Logic.fromString(logicSymbol.name)
        SetLogic(logic)
      }
      case Tokens.SetOption() => {
        SetOption(parseOption)
      }
      case Tokens.SetInfo() => {
        SetInfo(parseAttribute)
      }

      case Tokens.Assert() => ???

      case _ => sys.error("TODO")
    }
    eat(Tokens.CParen())

    cmd.setPos(head)
  }

  def parseInfoFlag: InfoFlag = {
    nextToken match {
      case Tokens.Keyword("error-behaviour") => ErrorBehaviourInfoFlag
      case Tokens.Keyword("name") => NameInfoFlag
      case Tokens.Keyword("authors") => AuthorsInfoFlag
      case Tokens.Keyword("version") => VersionInfoFlag
      case Tokens.Keyword("status") => StatusInfoFlag
      case Tokens.Keyword("reason-unknown") => ReasonUnknownInfoFlag
      case Tokens.Keyword("all-statistics") => AllStatisticsInfoFlag
      case Tokens.Keyword(keyword) => KeywordInfoFlag(keyword)
      case _ => sys.error("TODO")
    }
  }

  def parseAttribute: Attribute = {
    val keyword = parseKeyword
    val attributeValue = tryParseAttributeValue
    Attribute(keyword, attributeValue)
  }

  def tryParseAttributeValue: Option[SExpr] = {
    nextToken match {
      case Tokens.StringLit(s) => Some(SString(s))
      case Tokens.SymbolLit(s) => Some(SSymbol(s))
      case _ => None
    }
  }

  def parseOption: SMTOption = {
    peekToken match {
      case Tokens.Keyword("print-success") =>
        nextToken
        PrintSuccess(parseBool)
      case Tokens.Keyword("expand-definitions") => 
        nextToken
        ExpandDefinitions(parseBool)
      case Tokens.Keyword("interactive-mode") => 
        nextToken
        InteractiveMode(parseBool)
      case Tokens.Keyword("produce-proofs") => 
        nextToken
        ProduceProofs(parseBool)
      case Tokens.Keyword("produce-unsat-cores") => 
        nextToken
        ProduceUnsatCores(parseBool)
      case Tokens.Keyword("produce-models") => 
        nextToken
        ProduceModels(parseBool)
      case Tokens.Keyword("produce-assignments") => 
        nextToken
        ProduceAssignments(parseBool)
      case Tokens.Keyword("regular-output-channel") => 
        nextToken
        RegularOutputChannel(parseString.value)
      case Tokens.Keyword("diagnostic-output-channel") => 
        nextToken
        DiagnosticOutputChannel(parseString.value)
      case Tokens.Keyword("random-seed") => 
        nextToken
        RandomSeed(parseNumeral.value.toInt)
      case Tokens.Keyword("verbosity") => 
        nextToken
        Verbosity(parseNumeral.value.toInt)
      case _ => 
        AttributeOption(parseAttribute)
    }
  }

  def parseBool: Boolean = {
    nextToken match {
      case Tokens.SymbolLit("true") => true
      case Tokens.SymbolLit("false") => false
      case _ => sys.error("TODO")
    }
  }

  def parseString: SString = {
    nextToken match {
      case t@Tokens.StringLit(s) => {
        val str = SString(s)
        str.setPos(t)
      }
      case _ => sys.error("TODO")
    }
  }
  def parseSymbol: SSymbol = {
    nextToken match {
      case t@Tokens.SymbolLit(s) => {
        val symbol = SSymbol(s)
        symbol.setPos(t)
      }
      case _ => sys.error("TODO")
    }
  }

  def parseNumeral: SNumeral = {
    nextToken match {
      case t@Tokens.NumeralLit(n) => {
        val num = SNumeral(n)
        num.setPos(t)
      }
      case _ => sys.error("TODO")
    }
  }

  def parseKeyword: SKeyword = {
    nextToken match {
      case t@Tokens.Keyword(k) => {
        val keyword = SKeyword(k)
        keyword.setPos(t)
      }
      case _ => sys.error("TODO")
    }
  }



}

//object Parser {
//
//  class EOFBeforeMatchingParenthesisException(startPos: Position) extends
//    Exception("Opened parenthesis at position: " + startPos + " has no matching closing parenthesis")
//  class UnexpectedTokenException(token: Token, pos: Position) extends
//    Exception("Unexpected token: " + token + " at position: " + pos)
//
//  def fromString(str: String): Parser = {
//    val lexer = new Lexer(new java.io.StringReader(str))
//    new Parser(lexer)
//  }
//
//  def fromReader(reader: java.io.Reader): Parser = {
//    val lexer = new Lexer(reader)
//    new Parser(lexer)
//  }
//
//  /*
//   * Parse a string and return the next SExpr in the string, ignore the rest
//   */
//  def exprFromString(str: String): SExpr = {
//    val lexer = new Lexer(new java.io.StringReader(str))
//    val parser = new Parser(lexer)
//    parser.next
//  }
//}
//
//// vim: set ts=4 sw=4 et: