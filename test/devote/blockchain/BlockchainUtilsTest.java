package devote.blockchain;

import com.google.common.base.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reflections.Configuration;
import org.reflections.Reflections;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlockchainUtilsTest {
    @Mock
    private Reflections mockReflections;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindUniqueSubtypeOfOrNull_ReturnsNull() {
        // Given
        Configuration mockConfig = mock(Configuration.class);
        when(mockReflections.getSubTypesOf(any())).thenReturn(new HashSet<>());
        when(mockReflections.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getInputsFilter()).thenReturn(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return true;
            }

            @Override
            public String toString() {
                return "somepackage";
            }
        });

        // When
        Class foundClass = BlockchainUtils.findUniqueSubtypeOfOrNull(SomeClass.class, mockReflections);

        // Then
        assertThat(foundClass, is(nullValue()));
    }

    private static class SomeClass {
    }

}
