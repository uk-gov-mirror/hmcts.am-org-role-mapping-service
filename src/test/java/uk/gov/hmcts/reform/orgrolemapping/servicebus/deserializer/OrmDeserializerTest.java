package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;


class OrmDeserializerTest {

    private ObjectMapper mapper = new ObjectMapper();

    OrmDeserializer sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sut = new OrmDeserializer(mapper);
    }

    @Test
    void deserialize() {
        String deserializeMe = "4567";
        byte[] byteArray = deserializeMe.getBytes();
        List<byte[]> list = new ArrayList<>();
        list.add(byteArray);

        assertNotNull(sut.deserialize(list));
    }

    @Test
    void deserialize_throws() {
        String deserializeMe = "4dc7dd3c-3fb5-4611-bbde-5101a97681e0";
        byte[] byteArray = deserializeMe.getBytes();
        List<byte[]> list = new ArrayList<>();
        list.add(byteArray);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.deserialize(list);
        });
    }
}