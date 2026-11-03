package org.bytedeco.opencv.spring.boot.nd4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;

import org.bytedeco.opencv.spring.boot.dl4j.FaceNetSmallV2Model;
import org.bytedeco.opencv.spring.boot.nd4j.store.INDArrayLocalCacheStoreProvider;
import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Exercises the image embedding flow with real ND4J arrays on Linux CI. */
@EnabledOnOs(OS.LINUX)
class NativeEmbeddingPipelineTest {

    @TempDir
    Path tempDir;

    @Test
    void transposesChannelsAndComputesDistance() {
        INDArray channels = Nd4j.create(new double[] { 10, 20, 30 }).reshape(1, 3, 1);
        INDArray transposed = Nd4jUtils.transpose(channels, 1, 1);

        assertArrayEquals(new long[] { 1, 3, 1, 1 }, transposed.shape());
        assertEquals(30.0, transposed.getDouble(0, 0, 0, 0));
        assertEquals(20.0, transposed.getDouble(0, 1, 0, 0));
        assertEquals(10.0, transposed.getDouble(0, 2, 0, 0));
        assertEquals(3.0, Nd4jUtils.distance(Nd4j.create(new double[] { 1, 2 }),
                Nd4j.create(new double[] { 4, 2 })), 1e-10);
    }

    @Test
    void buildsAndInitializesFaceNetGraph() throws Exception {
        FaceNetSmallV2Model model = new FaceNetSmallV2Model();
        assertEquals(java.util.List.of("input1"), model.conf().getNetworkInputs());
        assertEquals(java.util.List.of("encodings"), model.conf().getNetworkOutputs());

        ComputationGraph graph = model.init();
        assertTrue(graph.numParams() > 0);
        assertEquals("encodings", graph.getConfiguration().getNetworkOutputs().get(0));
    }

    @Test
    void storesNormalizedEmbeddingFromAllInputForms() throws Exception {
        INDArray raw = Nd4j.create(new double[] { 0, 127.5, 255 }).reshape(1, 3, 1);
        INDArray dense = Nd4j.create(new double[] { 0.4, 0.8 });
        INDArray embedding = Nd4j.create(new double[] { 0.5, 0.5 });
        BaseImageLoader loader = mock(BaseImageLoader.class);
        ComputationGraph graph = mock(ComputationGraph.class);
        GraphVertex vertex = mock(GraphVertex.class);
        when(loader.asMatrix(any(java.io.InputStream.class))).thenReturn(raw);
        when(loader.asMatrix(any(File.class))).thenReturn(raw);
        when(graph.feedForward(any(INDArray.class), eq(false))).thenReturn(Map.of("dense", dense));
        when(graph.getVertex("encodings")).thenReturn(vertex);
        when(vertex.doForward(eq(false), any())).thenReturn(embedding);

        Nd4jTemplate template = new Nd4jTemplate(graph, loader, 1, 1);
        INDArrayLocalCacheStoreProvider store = new INDArrayLocalCacheStoreProvider();
        template.setStoreProvider(store);
        template.faceNew("team", "bytes", new byte[] { 1 });
        File file = tempDir.resolve("face.png").toFile();
        template.faceNew("team", "file", file);
        template.faceNew("team", "path", file.getAbsolutePath());

        assertSame(embedding, store.get("team", "bytes"));
        assertSame(embedding, store.get("team", "file"));
        assertSame(embedding, store.get("team", "path"));
        INDArray matrix = template.asMatrix(new ByteArrayInputStream(new byte[] { 1 }));
        assertEquals(255.0, matrix.getDouble(0, 0, 0, 0));
        assertEquals(127.5, matrix.getDouble(0, 1, 0, 0));
        assertEquals(0.0, matrix.getDouble(0, 2, 0, 0));
        assertArrayEquals(new long[] { 1, 3, 1, 1 }, matrix.shape());
        verify(vertex, org.mockito.Mockito.atLeastOnce()).setInputs(dense);
    }

    @Test
    void reportsBothMatchOutcomesFromEmbeddingDistance() throws Exception {
        INDArray raw = Nd4j.create(new double[] { 1, 2, 3 }).reshape(1, 3, 1);
        INDArray dense = Nd4j.create(new double[] { 1, 1 });
        BaseImageLoader loader = mock(BaseImageLoader.class);
        ComputationGraph graph = mock(ComputationGraph.class);
        GraphVertex vertex = mock(GraphVertex.class);
        when(loader.asMatrix(any(java.io.InputStream.class))).thenReturn(raw);
        when(graph.feedForward(any(INDArray.class), eq(false))).thenReturn(Map.of("dense", dense));
        when(graph.getVertex("encodings")).thenReturn(vertex);
        when(vertex.doForward(eq(false), any())).thenReturn(
                Nd4j.create(new double[] { 0, 0 }), Nd4j.create(new double[] { 0, 0 }),
                Nd4j.create(new double[] { 0, 0 }), Nd4j.create(new double[] { 1, 1 }));

        Nd4jTemplate template = new Nd4jTemplate(graph, loader, 1, 1);
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            template.match(new byte[] { 1 }, new byte[] { 2 });
            template.match(new byte[] { 1 }, new byte[] { 2 });
        } finally {
            System.setOut(original);
        }
        assertTrue(output.toString().contains("match"));
        assertTrue(output.toString().contains("dismatch"));
    }
}
