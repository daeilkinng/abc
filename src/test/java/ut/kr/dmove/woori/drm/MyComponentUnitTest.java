package ut.kr.dmove.woori.drm;

import org.junit.Test;
import kr.dmove.woori.drm.api.MyPluginComponent;
import kr.dmove.woori.drm.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}