////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2012 Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
////////////////////////////////////////////////////////////////////////////////

package com.github.sevntu.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * <p>Highlight usage redundant returns inside constructors and methods with void result.</p>
 * <p>
 * For example:
 * </p>
 * <p>1. Non empty constructor</p>
 * <code><pre>
 *    public HelloWorld(){
 *        doStuff();
 *        return;
 *    }</pre></code>
 *   
 * <p>2. Method with void result</p>
 *    <code><pre>
 *    public void testMethod1(){
 *        doStuff();
 *        return;
 *    }</pre></code>
 *   
 * <p>However, if your IDE does not support breakpoints on the method entry,
 * you can allow the use of redundant returns in constructors and methods
 * with void result without code except for 'return;'.</p>
 * <p>
 * For example:
 * </p>
 * <p>1. Empty constructor</p>
 *    <code><pre>
 *    public HelloWorld(){
 *        return;
 *    }</pre></code>
 * 
 * <p>2. Method with void result and empty body</p>
 *    <code><pre>
 *    public void testMethod1(){
 *        return;
 *    }</pre></code>
 *   
 * @author <a href="mailto:fishh1991@gmail.com">Troshin Sergey</a>
 * @author <a href="mailto:maxvetrenko2241@gmail.com">Max Vetrenko</a>
 */
public class RedundantReturnCheck extends Check {
	
	final static String MSG_REDUNDANT_RETURN = "redundant.return";

	// If True, allow 'return' in empty constructors and methods that return void.
	private boolean mAllowReturnInEmptyMethodsAndConstructors = false;

	public void setAllowReturnInEmptyMethodsAndConstructors(boolean aAllowEmptyBlocks)
	{
		mAllowReturnInEmptyMethodsAndConstructors = aAllowEmptyBlocks;
	}

	@Override
	public int[] getDefaultTokens()
	{
		return new int[] { TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF };
	}

	@Override
	public void visitToken(DetailAST aAst)
	{

		final DetailAST methodObjectBlock = aAst.getLastChild();

		switch (aAst.getType()) {
		case TokenTypes.CTOR_DEF:
			checkForRedundantReturn(methodObjectBlock);
			break;

		case TokenTypes.METHOD_DEF:
			if (aAst.findFirstToken(TokenTypes.TYPE).findFirstToken(
					TokenTypes.LITERAL_VOID) != null) {

				checkForRedundantReturn(methodObjectBlock);
			}
			break;

		default:
			final String exception = " Unexpected TokenType -  ";
			throw new IllegalStateException(exception + aAst.getText());
		}
	}

	/**
	 * Return is redundant if he is on the end of objectBlock and the
	 * objectBlock of the method divided into several tokens.
	 *
	 * @param aMethodObjectBlock
	 *            - a method or constructor object block
	 */
	private void checkForRedundantReturn(DetailAST aMethodObjectBlock)
	{

		final int methodChildCount = aMethodObjectBlock.getChildCount();

		if (methodChildCount != 1 && !containsInmplementation(aMethodObjectBlock)) {

			final int placeForRedundantReturn = aMethodObjectBlock
					.getLastChild().getPreviousSibling().getType();

			final int methodWithSingleChild = 2;

			if (methodChildCount > methodWithSingleChild) {

				handlePlacesForRedundantReturn(placeForRedundantReturn,
						aMethodObjectBlock);
			} else {

				if (!mAllowReturnInEmptyMethodsAndConstructors) {
					handlePlacesForRedundantReturn(placeForRedundantReturn,
							aMethodObjectBlock);
				}

				if (placeForRedundantReturn == TokenTypes.LITERAL_TRY) {
					submitRedundantReturnInTryCatch(aMethodObjectBlock
							.getFirstChild());
				}
			}
		}
	}

	/**
	 * Checks, if method is contained in interface.
	 * @param aMethodObjectBlock
	 *        method object block.
	 * @return
	 *       true, if method is contained in interface.
	 */
	private static boolean containsInmplementation(DetailAST aMethodObjectBlock)
	{
	    return aMethodObjectBlock.getType() == TokenTypes.SEMI;
	}

	/**
	 * @param aType
	 *            - Type of token, where redundant return is expected.
	 * @param aMethodObjectBlock
	 *            - A method or constructor object block.
	 */
	private void handlePlacesForRedundantReturn(int aType,
			DetailAST aMethodObjectBlock)
	{

		if (aType == TokenTypes.LITERAL_RETURN) {

			final DetailAST aLastChild = aMethodObjectBlock.getLastChild();

			log(aLastChild.getPreviousSibling().getLineNo());
		} else if (aType == TokenTypes.LITERAL_TRY) {
			submitRedundantReturnInTryCatch(
			        aMethodObjectBlock.findFirstToken(TokenTypes.LITERAL_TRY));
		}
	}

	/**
	 * Check the try, catch, finally object blocks on redundant return content.
	 *
	 * @param aTryAst
	 *            - Ast that contain a try node.
	 */
	private void submitRedundantReturnInTryCatch(DetailAST aTryAst)
	{

		DetailAST astBlockTry = aTryAst.getFirstChild();

		handleBlocksTryCatchFinally(astBlockTry.getLastChild()
				.getPreviousSibling());

		final int catchBlocksAmount = aTryAst
				.getChildCount(TokenTypes.LITERAL_CATCH);

		for (int i = 0; i < catchBlocksAmount; i++) {

			astBlockTry = astBlockTry.getNextSibling();
			handleBlocksTryCatchFinally(astBlockTry.getLastChild()
					.getLastChild().getPreviousSibling());
		}

		if (astBlockTry.getNextSibling() != null) {

			handleBlocksTryCatchFinally(astBlockTry.getNextSibling()
					.getLastChild().getLastChild().getPreviousSibling());

		}
	}

	/**
	 * Submit a mistake if the try or catch or finally blocks have redundant
	 * return.
	 *
	 * @param aAstReturn
	 *            a place where the redundantReturn is expected.
	 */
	private void handleBlocksTryCatchFinally(DetailAST aAstReturn)
	{

		if (aAstReturn != null) {

			if (aAstReturn.getType() == TokenTypes.LITERAL_RETURN) {

				log(aAstReturn.getLineNo());
			}
		}
	}

	private void log(int lineNumber)
	{
		log(lineNumber, MSG_REDUNDANT_RETURN);
	}
}
