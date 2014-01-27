package com.github.sevntu.checkstyle.checks.coding;

import static com.github.sevntu.checkstyle.checks.coding.RedundantReturnCheck.*;

import org.junit.Test;

import com.github.sevntu.checkstyle.BaseCheckTestSupport;
import com.github.sevntu.checkstyle.checks.coding.RedundantReturnCheck;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

public class RedundantReturnCheckTest extends BaseCheckTestSupport
{
    @Test
    public void testInputWithIgnoreEmptyConstructorsTrue()
            throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RedundantReturnCheck.class);
        checkConfig.addAttribute("allowReturnInEmptyMethodsAndConstructors",
                "false");

        final String[] expected = { 
        		"12: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "19: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "24: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "34: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "41: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "54: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "58: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "62: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "89: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "102: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "106: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "119: " + getCheckMessage(MSG_REDUNDANT_RETURN),
        };

        verify(checkConfig, getPath("InputRedundantReturn.java"), expected);
    }

    @Test
    public void testInputWithIgnoreEmptyConstructorsFalse()
            throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RedundantReturnCheck.class);
        checkConfig.addAttribute("allowReturnInEmptyMethodsAndConstructors",
                "true");

        final String[] expected = { 
        		"19: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "34: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "41: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "54: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "58: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "62: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "102: " + getCheckMessage(MSG_REDUNDANT_RETURN),
                "106: " + getCheckMessage(MSG_REDUNDANT_RETURN), 
                "119: " + getCheckMessage(MSG_REDUNDANT_RETURN),
        };

        verify(checkConfig, getPath("InputRedundantReturn.java"), expected);

    }

    @Test
    public void inputRedundantReturnInterface()
            throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RedundantReturnCheck.class);
        checkConfig.addAttribute("allowReturnInEmptyMethodsAndConstructors",
                "true");

        final String[] expected = {};

        verify(checkConfig, getPath("InputRedundantReturnInterface.java"),
                expected);

    }

    @Test
    public void inputRedundantReturnAbstractClass()
            throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RedundantReturnCheck.class);
        checkConfig.addAttribute("allowReturnInEmptyMethodsAndConstructors",
                "true");

        final String[] expected = {};

        verify(checkConfig, getPath("InputRedundantReturnAbstractClass.java"),
                expected);

    }
}
