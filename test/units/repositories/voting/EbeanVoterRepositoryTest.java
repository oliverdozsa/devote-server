package units.repositories.voting;

import data.repositories.imp.voting.EbeanVoterRepository;
import io.ebean.EbeanServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import security.TokenAuthUserIdUtil;

import static org.junit.Assert.assertThrows;

public class EbeanVoterRepositoryTest {
    private EbeanVoterRepository repository;

    @Mock
    private EbeanServer mockEbeanServer;

    @Mock
    private TokenAuthUserIdUtil mockIdCheckrer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        repository = new EbeanVoterRepository(mockEbeanServer, mockIdCheckrer);
    }

    @Test
    public void testEmailAndUserIdAreEmpty(){
        // Given
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> repository.userAuthenticated(null, null));
    }
}
