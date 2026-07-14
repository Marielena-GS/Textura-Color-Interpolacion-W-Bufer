package efectos.exposiciones.grupo3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Ejercicio Propuesto — Fundamentos de Renderizado en OpenGL
 *
 * Demuestra en un solo archivo:
 *   1. Color e interpolación baricéntrica entre vértices
 *   2. Textura con coordenadas UV aplicada a un triángulo
 *   3. Z-Buffer activable/desactivable (tecla Z)
 *   4. Doble buffering real con glfwSwapBuffers()
 *   5. Animación suave de rotación
 */
public class TriangulosDemo {

    private long window;
    private int shaderProgram;
    private int vaoColor, vboColor;
    private int vaoTex,   vboTex;
    private int textureId;
    private boolean zBufferOn = true;

    public static void main(String[] args) {
        new TriangulosDemo().run();
    }

    private void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY.set("lwjgl_natives");

        if (!glfwInit()) throw new IllegalStateException("No se pudo inicializar GLFW");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(800, 600, "Z-Buffer: ACTIVO", NULL, NULL);
        if (window == NULL) throw new RuntimeException("No se pudo crear la ventana");

        org.lwjgl.glfw.GLFWVidMode vidmode = org.lwjgl.glfw.GLFW.glfwGetVideoMode(org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor());
        if (vidmode != null) {
            org.lwjgl.glfw.GLFW.glfwSetWindowPos(
                window,
                (vidmode.width() - 800) / 2,
                (vidmode.height() - 600) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        shaderProgram = createShaders();
        setupTrianguloColor();
        setupTrianguloTextura();
        loadTexture("src/main/resources/textura.jpg");
        setupTextRenderer();
    }

    private int createShaders() {

        String vert = "#version 330 core\n"
                + "layout(location=0) in vec3 aPos;\n"
                + "layout(location=1) in vec3 aColor;\n"
                + "layout(location=2) in vec2 aUV;\n"
                + "out vec3 vColor;\n"
                + "out vec2 vUV;\n"
                + "uniform mat4 uModel;\n"
                + "void main() {\n"
                + "    gl_Position = uModel * vec4(aPos, 1.0);\n"
                + "    vColor = aColor;\n"
                + "    vUV = aUV;\n"
                + "}\n";

        String frag = "#version 330 core\n"
                + "in vec3 vColor;\n"
                + "in vec2 vUV;\n"
                + "out vec4 FragColor;\n"
                + "uniform sampler2D uTex;\n"
                + "uniform int useTexture;\n"
                + "void main() {\n"
                + "    if (useTexture == 1)\n"
                + "        FragColor = texture(uTex, vUV);\n"
                + "    else\n"
                + "        FragColor = vec4(vColor, 1.0);\n"
                + "}\n";

        int vs = compileShader(GL_VERTEX_SHADER, vert);
        int fs = compileShader(GL_FRAGMENT_SHADER, frag);
        int prog = glCreateProgram();
        glAttachShader(prog, vs);
        glAttachShader(prog, fs);
        glLinkProgram(prog);
        glDeleteShader(vs);
        glDeleteShader(fs);
        return prog;
    }

    private int compileShader(int type, String src) {
        int s = glCreateShader(type);
        glShaderSource(s, src);
        glCompileShader(s);
        if (glGetShaderi(s, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Error shader: " + glGetShaderInfoLog(s));
        return s;
    }

    private void setupTrianguloColor() {

        float[] v = {
             0.0f,  0.6f, -0.1f,   1.0f, 0.0f, 0.0f,   0.5f, 1.0f,
            -0.6f, -0.4f, -0.1f,   0.0f, 1.0f, 0.0f,   0.0f, 0.0f,
             0.6f, -0.4f, -0.1f,   0.0f, 0.0f, 1.0f,   1.0f, 0.0f
        };
        vaoColor = glGenVertexArrays();
        vboColor = glGenBuffers();
        uploadVertices(vaoColor, vboColor, v);
    }

    private void setupTrianguloTextura() {

        float[] v = {
             0.0f,  0.4f,  0.1f,   1.0f, 1.0f, 1.0f,   0.5f, 1.0f,
            -0.4f, -0.3f,  0.1f,   1.0f, 1.0f, 1.0f,   0.0f, 0.0f,
             0.4f, -0.3f,  0.1f,   1.0f, 1.0f, 1.0f,   1.0f, 0.0f
        };
        vaoTex = glGenVertexArrays();
        vboTex = glGenBuffers();
        uploadVertices(vaoTex, vboTex, v);
    }

    private void uploadVertices(int vao, int vbo, float[] data) {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buf = MemoryUtil.memAllocFloat(data.length);
        buf.put(data).flip();
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        MemoryUtil.memFree(buf);

        int stride = 8 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    private void loadTexture(String path) {
        STBImage.stbi_set_flip_vertically_on_load(true);
        int[] w = new int[1], h = new int[1], ch = new int[1];
        ByteBuffer pixels = STBImage.stbi_load(path, w, h, ch, 4);
        
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        if (pixels == null) {
            System.err.println("Advertencia: No se pudo cargar textura " + path + ". Usando textura por defecto.");
            ByteBuffer defaultTex = MemoryUtil.memAlloc(4);
            defaultTex.put((byte) 255).put((byte) 0).put((byte) 255).put((byte) 255).flip();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, defaultTex);
            MemoryUtil.memFree(defaultTex);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w[0], h[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            glGenerateMipmap(GL_TEXTURE_2D);
            STBImage.stbi_image_free(pixels);
        }
    }

    private void loop() {
        float angle = 0;
        double lastToggle = 0;

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();

            if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
                glfwSetWindowShouldClose(window, true);

            if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS && now - lastToggle > 0.3) {
                zBufferOn = !zBufferOn;
                lastToggle = now;
                if (zBufferOn) {
                    glEnable(GL_DEPTH_TEST);
                    glfwSetWindowTitle(window, "Z-Buffer: ACTIVO — el triángulo de frente tapa al de atrás");
                    System.out.println("Z-Buffer ACTIVADO");
                } else {
                    glDisable(GL_DEPTH_TEST);
                    glfwSetWindowTitle(window, "Z-Buffer: DESACTIVADO — Algoritmo del Pintor");
                    System.out.println("Z-Buffer DESACTIVADO (Algoritmo del Pintor)");
                }
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(shaderProgram);

            angle += 0.01f;
            Matrix4f model = new Matrix4f().rotateY(angle);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "uModel"),
                    false, model.get(new float[16]));

            if (zBufferOn) {
                drawColor();
                drawTextura();
            } else {
                drawTextura();
                drawColor();
            }
            
            drawTextHUD();
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void drawColor() {
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        glBindVertexArray(vaoColor);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void drawTextura() {
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glBindVertexArray(vaoTex);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void cleanup() {
        glDeleteVertexArrays(vaoColor);
        glDeleteBuffers(vboColor);
        glDeleteVertexArrays(vaoTex);
        glDeleteBuffers(vboTex);
        glDeleteVertexArrays(textVAO);
        glDeleteBuffers(textVBO);
        glDeleteTextures(textureId);
        glDeleteProgram(shaderProgram);
        glDeleteProgram(textShaderProgram);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
    private int textShaderProgram;
    private int textVAO, textVBO;

    private void setupTextRenderer() {
        String vert = "#version 330 core\n"
            + "layout(location=0) in vec2 aPos;\n"
            + "void main() {\n"
            + "    gl_Position = vec4(aPos, 0.0, 1.0);\n"
            + "}\n";

        String frag = "#version 330 core\n"
            + "out vec4 FragColor;\n"
            + "void main() {\n"
            + "    FragColor = vec4(1.0, 0.88, 0.2, 1.0);\n"
            + "}\n";

        int vs = compileShader(org.lwjgl.opengl.GL20.GL_VERTEX_SHADER, vert);
        int fs = compileShader(org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER, frag);
        textShaderProgram = org.lwjgl.opengl.GL20.glCreateProgram();
        org.lwjgl.opengl.GL20.glAttachShader(textShaderProgram, vs);
        org.lwjgl.opengl.GL20.glAttachShader(textShaderProgram, fs);
        org.lwjgl.opengl.GL20.glLinkProgram(textShaderProgram);
        org.lwjgl.opengl.GL20.glDeleteShader(vs);
        org.lwjgl.opengl.GL20.glDeleteShader(fs);

        textVAO = org.lwjgl.opengl.GL30.glGenVertexArrays();
        textVBO = org.lwjgl.opengl.GL15.glGenBuffers();
    }

    private void drawTextHUD() {
        String title = "=== MOTOR OPENGL (GRUPO 3) ===";
        String controls = "Controles:\n"
                        + "[ Z ]   Alternar Z-Buffer\n"
                        + "[ ESC ] Cerrar Motor Nativo\n\n"
                        + "ESTADO ACTUAL:\n"
                        + "> Z-Buffer: " + (zBufferOn ? "ACTIVADO (Interseccion Correcta)" : "DESACTIVADO (Algoritmo Pintor)");

        ByteBuffer charBufferTitle = org.lwjgl.system.MemoryUtil.memAlloc(title.length() * 300);
        int numQuadsTitle = org.lwjgl.stb.STBEasyFont.stb_easy_font_print(0, 0, title, null, charBufferTitle);

        ByteBuffer charBufferControls = org.lwjgl.system.MemoryUtil.memAlloc(controls.length() * 300);
        int numQuadsControls = org.lwjgl.stb.STBEasyFont.stb_easy_font_print(0, 0, controls, null, charBufferControls);

        int totalQuads = numQuadsTitle + numQuadsControls;
        FloatBuffer vertexData = org.lwjgl.system.MemoryUtil.memAllocFloat(totalQuads * 6 * 2);
        
        float scale = 1.8f;
        
        // Agregar vertices del titulo (Arriba)
        addTextVertices(vertexData, charBufferTitle, numQuadsTitle, scale, 20.0f, 20.0f);
        
        // Agregar vertices de controles (Abajo)
        addTextVertices(vertexData, charBufferControls, numQuadsControls, scale, 20.0f, 480.0f);

        vertexData.flip();

        org.lwjgl.opengl.GL20.glUseProgram(textShaderProgram);
        org.lwjgl.opengl.GL30.glBindVertexArray(textVAO);
        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, textVBO);
        org.lwjgl.opengl.GL15.glBufferData(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, vertexData, org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW);
        org.lwjgl.opengl.GL20.glVertexAttribPointer(0, 2, org.lwjgl.opengl.GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        org.lwjgl.opengl.GL20.glEnableVertexAttribArray(0);

        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        org.lwjgl.opengl.GL11.glDrawArrays(org.lwjgl.opengl.GL11.GL_TRIANGLES, 0, totalQuads * 6);
        if(zBufferOn) org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);

        org.lwjgl.system.MemoryUtil.memFree(vertexData);
        org.lwjgl.system.MemoryUtil.memFree(charBufferTitle);
        org.lwjgl.system.MemoryUtil.memFree(charBufferControls);
    }
    
    private void addTextVertices(FloatBuffer vertexData, ByteBuffer charBuffer, int numQuads, float scale, float startX, float startY) {
        for(int i = 0; i < numQuads; i++) {
            int offset = i * 4 * 16;
            
            float x0 = charBuffer.getFloat(offset + 0) * scale + startX;
            float y0 = charBuffer.getFloat(offset + 4) * scale + startY;
            
            float x1 = charBuffer.getFloat(offset + 16 + 0) * scale + startX;
            float y1 = charBuffer.getFloat(offset + 16 + 4) * scale + startY;
            
            float x2 = charBuffer.getFloat(offset + 32 + 0) * scale + startX;
            float y2 = charBuffer.getFloat(offset + 32 + 4) * scale + startY;
            
            float x3 = charBuffer.getFloat(offset + 48 + 0) * scale + startX;
            float y3 = charBuffer.getFloat(offset + 48 + 4) * scale + startY;
            
            float nx0 = (x0 / 400.0f) - 1.0f; float ny0 = 1.0f - (y0 / 300.0f);
            float nx1 = (x1 / 400.0f) - 1.0f; float ny1 = 1.0f - (y1 / 300.0f);
            float nx2 = (x2 / 400.0f) - 1.0f; float ny2 = 1.0f - (y2 / 300.0f);
            float nx3 = (x3 / 400.0f) - 1.0f; float ny3 = 1.0f - (y3 / 300.0f);

            vertexData.put(nx0).put(ny0);
            vertexData.put(nx1).put(ny1);
            vertexData.put(nx2).put(ny2);
            
            vertexData.put(nx2).put(ny2);
            vertexData.put(nx3).put(ny3);
            vertexData.put(nx0).put(ny0);
        }
    }
}
