////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright(C) 2001-2012  Oliver Burn
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.github.sevntu.checkstyle.checks.coding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * <p>
 * This check verifies the name of JUnit4 test class for compliance with given
 * naming convention. Usually class can be named "Test*", "*Test", "*TestCase"
 * or "*IT" (the latter is for integration tests), but you can provide your own
 * regexp to match 'valid' names of UTs classes.
 * </p>
 * <p>
 * Processing rules:
 * </p>
 * <p>
 * If class contains at least one method annotated with @Test or if user provide
 * own regexp JUnit test annotations (in the field jUnitTestAnnotationsRegex )
 * for the methods and classes, default: "RunWith", then the current class is
 * considered JUnit4 test class.
 * </p>
 * @author <a href="mailto:denant0vz@gmail.com">Denis Antonenkov</a>
 */

public class NameConvensionForJUnit4TestsClassesCheck extends Check
{
    /**
     * The key is pointing to the message text String in
     * "messages.properties file".
     */
    public static final String MSG_KEY = "name.convension.for.tests.classes";

    /**
     * Pattern object is used to store the regexp for the names of classes, that
     * could be named "Test*", "*Test", "*TestCase" or "*IT", but you can
     * provide you own regexp to match 'valid' names of UTs classes.
     */
    private Pattern mValidTestClassNameRegex = Pattern
            .compile(".+Test|Test.+|.+IT|.+TestCase");

    /**
     * Pattern object is used to store the regexp for the annotations, that
     * could be "RunWith", but you can provide you own regexp to match
     * annotations of UTs.
     */
    private Pattern mJUnitTestAnnotationsRegex = Pattern.compile("RunWith");

    /**
     * True, if is needed processing for the current node.
     */
    private boolean mProcessCurrentNode = true;

    /**
     * A current ClassDef AST is being processed by check.
     */
    private DetailAST mCurrentClassNode;

    /**
     * Sets 'valid' class name regexp for Uts.
     * @param aValidTestClassNameRegex
     *        regexp to match 'valid' unit test class names.
     */
    public void setValidTestClassNameRegex(
            String aValidTestClassNameRegex)
    {
        if (aValidTestClassNameRegex != null) {
            mValidTestClassNameRegex = Pattern
                    .compile(aValidTestClassNameRegex);
        }
    }

    /**
     * Sets annotations regexp for JUnits classes.
     * @param aJUnitTestAnnotationsRegex
     *        regexp to match annotations for unit test classes.
     */
    public void setJUnitTestAnnotationsRegex(
            String aJUnitTestAnnotationsRegex)
    {
        if (aJUnitTestAnnotationsRegex != null) {
            mJUnitTestAnnotationsRegex = Pattern.
                    compile(aJUnitTestAnnotationsRegex);
        }
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF, };
    }

    @Override
    public void visitToken(DetailAST aNode)
    {
        if (mProcessCurrentNode) {
            switch (aNode.getType()) {
            case TokenTypes.CLASS_DEF:
                if (mCurrentClassNode == null) {
                    if (hasAnnotation(aNode, mJUnitTestAnnotationsRegex)) {
                        if (hasWrongName(aNode)) {
                            logWrongTestClassName(aNode);
                        }
                        mProcessCurrentNode = false;
                    }
                    else {
                        mCurrentClassNode = aNode;
                    }
                }
                break;
            case TokenTypes.METHOD_DEF:
                if (hasCurrentClassNodeAsParent(aNode)
                        && hasAnnotation(aNode, mJUnitTestAnnotationsRegex))
                {
                    if (hasWrongName(mCurrentClassNode)) {
                        logWrongTestClassName(mCurrentClassNode);
                    }
                    mProcessCurrentNode = false;
                }
                break;
            default:
                throw new IllegalArgumentException("Node of type "
                        + aNode.getType() + " is not implemented");
            }
        }
    }

    @Override
    public void finishTree(DetailAST aRootAST)
    {
        mCurrentClassNode = null;
        mProcessCurrentNode = true;
    }

    /**
     * Returns true, if current class is a parent of aMethodDefNode.
     * @param aMethodDefNode
     *        the node of method definition.
     * @return True, if current class is a parent of aMethodDefNode.
     */
    private boolean hasCurrentClassNodeAsParent(DetailAST aMethodDefNode)
    {
        return aMethodDefNode.getParent().getParent().equals(mCurrentClassNode);
    }

    /**
     * Returns true, if the class is not the correct name.
     * @param aJUnitTestClassDefNode
     *        the node of class JUnit test.
     * @return True, if the class is not the correct name.
     */
    private boolean hasWrongName(final DetailAST aJUnitTestClassDefNode)
    {
        final String className = getIdentText(aJUnitTestClassDefNode);
        return !mValidTestClassNameRegex.matcher(className).matches();
    }

    /**
     * Returns true, if the class or method contains one of the annotations,
     * specified in the regexp, or Test (if the current node method).
     * @param aMethodOrClassDefNode
     *        the node of method or class definition.
     * @param aJUnitAnnotationsRegexp
     *        regexp contains JUnit test annotations
     * @return True, if the class or method contains one of the annotations,
     *         specified in the regexp, or Test (if the current node method).
     */
    private boolean hasAnnotation(DetailAST aMethodOrClassDefNode,
            Pattern aJUnitAnnotationsRegexp)
    {
        final DetailAST modifiersNode = aMethodOrClassDefNode
                .findFirstToken(TokenTypes.MODIFIERS);
        boolean hasAnnotation = false;
        if (modifiersNode.branchContains(TokenTypes.ANNOTATION)) {
            final List<DetailAST> allAnnotations = getAllAnnotations(
                    modifiersNode);
            for (DetailAST annotationNode : allAnnotations) {
                final DetailAST dotNode = annotationNode
                        .findFirstToken(TokenTypes.DOT);
                String annotationName = "";
                if (dotNode == null) {
                    annotationName = getIdentText(annotationNode);
                }
                else {
                    annotationName = getIdentText(dotNode);
                }
                if (aMethodOrClassDefNode.getType() == TokenTypes.METHOD_DEF) {
                    hasAnnotation = "Test".equals(annotationName);
                }
                hasAnnotation = hasAnnotation
                        || aJUnitAnnotationsRegexp.matcher(annotationName)
                                .matches();
                if (hasAnnotation) {
                    break;
                }
            }
        }
        return hasAnnotation;
    }

    /**
     * This method return all annotations, that contains node.
     * @param aClassOrMethodDefNode
     *        the node of class or method definition.
     * @return all annotations that contains node
     */
    private static List<DetailAST> getAllAnnotations(
            DetailAST aClassOrMethodDefNode)
    {
        final List<DetailAST> allAnnotations = new ArrayList<DetailAST>();
        DetailAST annotation = aClassOrMethodDefNode
                .findFirstToken(TokenTypes.ANNOTATION);
        while (annotation != null
                && annotation.getType() == TokenTypes.ANNOTATION)
        {
            allAnnotations.add(annotation);
            annotation = annotation.getNextSibling();
        }
        return allAnnotations;
    }

    /**
     * This method generates an error message for the specified class.
     * @param aClassDef
     *        the node of class definition.
     */
    private void logWrongTestClassName(DetailAST aClassDef)
    {
        log(aClassDef.findFirstToken(TokenTypes.LITERAL_CLASS)
                .getLineNo(), MSG_KEY, mValidTestClassNameRegex);
    }

    /**
     * Returns the text identifier for the node containing the identifier.
     * @param aNodeWithIdent
     *        the node containing identifier.
     * @return Returns the text identifier for the node containing the
     *         identifier.
     */
    private static String getIdentText(DetailAST aNodeWithIdent)
    {
        return aNodeWithIdent.findFirstToken(TokenTypes.IDENT).getText();
    }
}
