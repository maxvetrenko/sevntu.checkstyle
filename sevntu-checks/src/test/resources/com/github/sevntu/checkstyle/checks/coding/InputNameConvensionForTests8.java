package com.github.sevntu.checkstyle.checks.annotation;
import java.awt.Component;
import java.io.IOException;

@org.springframework.stereotype.Component
@org.apache.pakage.custom.Annotation("value")
public class StreamingLogger extends java.io.OutputStream
{

    private final Log log;

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private String enc;

    public StreamingLogger(Log log, String enc)
    {
        this.log = log;
        this.enc = enc;
    }

    public StreamingLogger(Log log)
    {
        this(log, null);
    }
    
    @Test
    public void teset(){
        
    }

    @Override
    public void write(int b)
            throws IOException
    {
        if (b == '\r')
        {
            // ignore
        }
        else if (b == '\n')
        {
            if (buffer.size() != 0)
            {
                String s;
                if (enc != null)
                {
                    s = buffer.toString(enc);
                }
                else
                {
                    s = buffer.toString();
                }
                buffer.reset();
                log.error(s);
            }
        }
        else
        {
            buffer.write(b);
        }
    }

}
