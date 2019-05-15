package org.testng;

import static org.testng.internal.EclipseInterface.ASSERT_LEFT;
import static org.testng.internal.EclipseInterface.ASSERT_LEFT2;
import static org.testng.internal.EclipseInterface.ASSERT_MIDDLE;
import static org.testng.internal.EclipseInterface.ASSERT_RIGHT;
import static org.testng.internal.EclipseInterface.ASSERT_LEFT_INEQUALITY;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.collections.Lists;

/**
 * Assertion tool class. Presents assertion methods with a more natural parameter order. The order
 * is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class RefactorCodeCloneExample {

    public static final String ARRAY_MISMATCH_TEMPLATE =
            "arrays differ firstly at element [%d]; " + "expected value is <%s> but was <%s>. %s";

    /** Protect constructor since it is a static only class */
    protected RefactorCodeCloneExample() {
        // hide constructor
    }




}
