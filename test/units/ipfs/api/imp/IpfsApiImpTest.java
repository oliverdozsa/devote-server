package units.ipfs.api.imp;

import com.fasterxml.jackson.databind.JsonNode;
import devote.blockchain.api.BlockchainException;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.multihash.Multihash;
import ipfs.api.imp.IpfsApiImp;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import play.libs.Json;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class IpfsApiImpTest {
    @Mock
    private IPFS mockIpfs;

    private IpfsApiImp ipfsApiImp;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ipfsApiImp = new IpfsApiImp(mockIpfs);
    }

    @Test
    public void testSaveJson() throws NoSuchFieldException, IllegalAccessException, IOException {
        // Given
        JsonNode someJson = Json.parse("{\"someField\": 42}");
        byte[] jsonBytes = someJson.toString().getBytes();

        Field dagField = IPFS.class.getField("dag");
        dagField.setAccessible(true);

        IPFS.Dag mockDag = Mockito.mock(IPFS.Dag.class);
        dagField.set(mockIpfs, mockDag);

        MerkleNode mockMerkleNode = Mockito.mock(MerkleNode.class);

        Field multihashField = MerkleNode.class.getField("hash");
        multihashField.setAccessible(true);

        Multihash mockMultihash = Mockito.mock(Multihash.class);
        multihashField.set(mockMerkleNode, mockMultihash);

        when(mockDag.put("json", jsonBytes)).thenReturn(mockMerkleNode);
        when(mockMultihash.getType()).thenReturn(Multihash.Type.md5);

        byte[] md5MockBytes = new byte[Multihash.Type.md5.length];
        when(mockMultihash.getHash()).thenReturn(md5MockBytes);

        // When
        String cid = ipfsApiImp.saveJson(someJson);

        // Then
        assertThat(cid, notNullValue());
        assertThat(cid, not(isEmptyString()));
    }

    @Test
    public void testSaveJson_Fail() throws IOException, IllegalAccessException, NoSuchFieldException {
        // Given
        JsonNode someJson = Json.parse("{\"someField\": 42}");
        byte[] jsonBytes = someJson.toString().getBytes();

        Field dagField = IPFS.class.getField("dag");
        dagField.setAccessible(true);

        IPFS.Dag mockDag = Mockito.mock(IPFS.Dag.class);
        dagField.set(mockIpfs, mockDag);

        when(mockDag.put("json", jsonBytes)).thenThrow(new IOException("Some IO"));

        // When
        // Then
        BlockchainException exception = assertThrows(BlockchainException.class, () -> ipfsApiImp.saveJson(someJson));

        assertThat(exception.getMessage(), equalTo("Failed to store json in IPFS."));
    }

    @Test
    public void retrieveJson() throws NoSuchFieldException, IllegalAccessException, IOException {
        // Given
        Field dagField = IPFS.class.getField("dag");
        dagField.setAccessible(true);

        IPFS.Dag mockDag = Mockito.mock(IPFS.Dag.class);
        dagField.set(mockIpfs, mockDag);

        JsonNode someJson = Json.parse("{\"someField\": 42}");
        byte[] jsonBytes = someJson.toString().getBytes();
        when(mockDag.get(ArgumentMatchers.any())).thenReturn(jsonBytes);

        // When
        JsonNode retrievedJson = ipfsApiImp.retrieveJson("z69qcZbRySVQbYq2ViXLs93PYA1G3");

        // Then
        assertThat(retrievedJson, notNullValue());
        assertThat(retrievedJson.get("someField").asInt(), Matchers.equalTo(42));
    }

    @Test
    public void retrieveJson_Fail() throws NoSuchFieldException, IllegalAccessException, IOException {
        // Given
        Field dagField = IPFS.class.getField("dag");
        dagField.setAccessible(true);

        IPFS.Dag mockDag = Mockito.mock(IPFS.Dag.class);
        dagField.set(mockIpfs, mockDag);

        when(mockDag.get(ArgumentMatchers.any())).thenThrow(new IOException("Some IO Exception"));

        // When
        BlockchainException exception = assertThrows(BlockchainException.class, () -> ipfsApiImp.retrieveJson("z69qcZbRySVQbYq2ViXLs93PYA1G3"));
        assertThat(exception.getMessage(), equalTo("Failed to read json with cid = z69qcZbRySVQbYq2ViXLs93PYA1G3 from IPFS."));
    }
}
