package com.modularwarfare.loader;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.api.model.ObjModelRenderer;
import com.modularwarfare.loader.part.Face;
import com.modularwarfare.loader.part.ModelObject;
import com.modularwarfare.loader.part.TextureCoordinate;
import com.modularwarfare.loader.part.Vertex;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Wavefront ModelObject importer
 * Based heavily off of the specifications found at http://en.wikipedia.org/wiki/Wavefront_.obj_file
 */
public class ObjModelBuilder {
    private static Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
    private static Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
    private static Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+(\\.\\d+)?){2,3} *$)");
    private static Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
    private static Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
    private static Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

    private static Matcher vertexMatcher, vertexNormalMatcher, textureCoordinateMatcher;
    private static Matcher face_V_VT_VN_Matcher, face_V_VT_Matcher, face_V_VN_Matcher, face_V_Matcher;
    private static Matcher groupObjectMatcher;

    public ArrayList<Vertex> vertices = new ArrayList<>();
    private ArrayList<Vertex> vertexNormals = new ArrayList<>();
    private ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<>();
    private ModelObject currentModelObject;

    private String fileLocation;
    private ResourceLocation resourceLocation;

    private ArrayList<ObjModelRenderer> renderers = new ArrayList<>();

    public ObjModelBuilder(String rl) throws ModelFormatException {
        this.fileLocation = rl;
    }

    public ObjModelBuilder(ResourceLocation rl) throws ModelFormatException {
        this.resourceLocation = rl;
    }

    /***
     * Verifies that the given line from the staticModel file is a valid vertex
     * @param line the line being validated
     * @return true if the line is a valid vertex, false otherwise
     */
    private static boolean isValidVertexLine(String line) {
        if (vertexMatcher != null) {
            vertexMatcher.reset();
        }

        vertexMatcher = vertexPattern.matcher(line);
        return vertexMatcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid vertex normal
     * @param line the line being validated
     * @return true if the line is a valid vertex normal, false otherwise
     */
    private static boolean isValidVertexNormalLine(String line) {
        if (vertexNormalMatcher != null) {
            vertexNormalMatcher.reset();
        }

        vertexNormalMatcher = vertexNormalPattern.matcher(line);
        return vertexNormalMatcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid texture coordinate
     * @param line the line being validated
     * @return true if the line is a valid texture coordinate, false otherwise
     */
    private static boolean isValidTextureCoordinateLine(String line) {
        if (textureCoordinateMatcher != null) {
            textureCoordinateMatcher.reset();
        }

        textureCoordinateMatcher = textureCoordinatePattern.matcher(line);
        return textureCoordinateMatcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid face that is described by vertices, texture coordinates, and vertex normals
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1/vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_VN_Line(String line) {
        if (face_V_VT_VN_Matcher != null) {
            face_V_VT_VN_Matcher.reset();
        }

        face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(line);
        return face_V_VT_VN_Matcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid face that is described by vertices and texture coordinates
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_Line(String line) {
        if (face_V_VT_Matcher != null) {
            face_V_VT_Matcher.reset();
        }

        face_V_VT_Matcher = face_V_VT_Pattern.matcher(line);
        return face_V_VT_Matcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid face that is described by vertices and vertex normals
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1//vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VN_Line(String line) {
        if (face_V_VN_Matcher != null) {
            face_V_VN_Matcher.reset();
        }

        face_V_VN_Matcher = face_V_VN_Pattern.matcher(line);
        return face_V_VN_Matcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid face that is described by only vertices
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_Line(String line) {
        if (face_V_Matcher != null) {
            face_V_Matcher.reset();
        }

        face_V_Matcher = face_V_Pattern.matcher(line);
        return face_V_Matcher.matches();
    }

    /***
     * Verifies that the given line from the staticModel file is a valid face of any of the possible face formats
     * @param line the line being validated
     * @return true if the line is a valid face that matches any of the valid face formats, false otherwise
     */
    private static boolean isValidFaceLine(String line) {
        return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
    }

    /***
     * Verifies that the given line from the staticModel file is a valid group (or object)
     * @param line the line being validated
     * @return true if the line is a valid group (or object), false otherwise
     */
    private static boolean isValidGroupObjectLine(String line) {
        if (groupObjectMatcher != null) {
            groupObjectMatcher.reset();
        }

        groupObjectMatcher = groupObjectPattern.matcher(line);
        return groupObjectMatcher.matches();
    }


    public ObjModel loadModelFromZIP(BaseType baseType) throws ModelFormatException {


        int lineCount = 0;
        ObjModel model = new ObjModel();
        boolean found = false;

        if (ModularWarfare.zipContentsPack.containsKey(baseType.contentPack)) {

            if (ModularWarfare.zipContentsPack.get(baseType.contentPack).models_cache.containsKey(fileLocation)) {
                return ModularWarfare.zipContentsPack.get(baseType.contentPack).models_cache.get(fileLocation);
            }

            FileHeader foundFile = ModularWarfare.zipContentsPack.get(baseType.contentPack).fileHeaders.stream().filter(fileHeader -> fileHeader.getFileName().equalsIgnoreCase(fileLocation)).findFirst().orElse(null);

            if (foundFile != null) {
                found = true;
                ZipInputStream stream = null;
                try {
                    stream = ModularWarfare.zipContentsPack.get(baseType.contentPack).getZipFile().getInputStream(foundFile);

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String currentLine;

                        while ((currentLine = reader.readLine()) != null) {
                            lineCount++;
                            currentLine = currentLine.replaceAll("\\s+", " ").trim();

                            if (currentLine.startsWith("#") || currentLine.length() == 0) {
                                continue;
                            } else if (currentLine.startsWith("v ")) {
                                Vertex vertex = parseVertex(currentLine, lineCount);
                                if (vertex != null) {
                                    vertices.add(vertex);
                                }
                            } else if (currentLine.startsWith("vn ")) {
                                Vertex vertex = parseVertexNormal(currentLine, lineCount);
                                if (vertex != null) {
                                    vertexNormals.add(vertex);
                                }
                            } else if (currentLine.startsWith("vt ")) {
                                TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                                if (textureCoordinate != null) {
                                    textureCoordinates.add(textureCoordinate);
                                }
                            } else if (currentLine.startsWith("f ")) {

                                if (currentModelObject == null) {
                                    currentModelObject = new ModelObject("Default");
                                }

                                Face face = parseFace(currentLine, lineCount);

                                if (face != null) {
                                    currentModelObject.faces.add(face);
                                }
                            } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                                ModelObject group = parseGroupObject(currentLine, lineCount);
                                if (group != null) {
                                    if (currentModelObject != null) {
                                        renderers.add(new ObjModelRenderer(model, currentModelObject));
                                    }
                                }

                                currentModelObject = group;
                            }
                        }
                        renderers.add(new ObjModelRenderer(model, currentModelObject));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
            ModularWarfare.zipContentsPack.get(baseType.contentPack).models_cache.put(fileLocation, model);
        }

        if (!found)
            ModularWarfare.LOGGER.warn("The model file in " + baseType.contentPack + " at: " + fileLocation + " has not been found");


        model.setParts(renderers);
        return model;
    }

    public ObjModel loadModelFromRL() throws ModelFormatException {

        int lineCount = 0;
        ObjModel model = new ObjModel();

        try (IResource objFile = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(objFile.getInputStream()))) {
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    lineCount++;
                    currentLine = currentLine.replaceAll("\\s+", " ").trim();

                    if (currentLine.startsWith("#") || currentLine.length() == 0) {
                        continue;
                    } else if (currentLine.startsWith("v ")) {
                        Vertex vertex = parseVertex(currentLine, lineCount);
                        if (vertex != null) {
                            vertices.add(vertex);
                        }
                    } else if (currentLine.startsWith("vn ")) {
                        Vertex vertex = parseVertexNormal(currentLine, lineCount);
                        if (vertex != null) {
                            vertexNormals.add(vertex);
                        }
                    } else if (currentLine.startsWith("vt ")) {
                        TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                        if (textureCoordinate != null) {
                            textureCoordinates.add(textureCoordinate);
                        }
                    } else if (currentLine.startsWith("f ")) {

                        if (currentModelObject == null) {
                            currentModelObject = new ModelObject("Default");
                        }

                        Face face = parseFace(currentLine, lineCount);

                        if (face != null) {
                            currentModelObject.faces.add(face);
                        }
                    } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                        ModelObject group = parseGroupObject(currentLine, lineCount);

                        if (group != null) {
                            if (currentModelObject != null) {
                                renderers.add(new ObjModelRenderer(model, currentModelObject));
                            }
                        }

                        currentModelObject = group;
                    }
                }

                renderers.add(new ObjModelRenderer(model, currentModelObject));
            }
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        }

        model.setParts(renderers);
        return model;
    }


    public ObjModel loadModel() throws ModelFormatException {

        int lineCount = 0;
        ObjModel model = new ObjModel();

        File modelFile = null;
        String absPath = new File(Loader.instance().getConfigDir().getParent(), "ModularWarfare").getAbsolutePath();

        if (!absPath.endsWith("/") || !absPath.endsWith("\\"))
            absPath += "/";
        modelFile = checkValidPath(absPath + fileLocation);

        if (modelFile == null || !modelFile.exists()) {
            ModularWarfare.LOGGER.info("The staticModel with the name " + fileLocation + " does not exist.");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(modelFile))) {
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                lineCount++;
                currentLine = currentLine.replaceAll("\\s+", " ").trim();

                if (currentLine.startsWith("#") || currentLine.length() == 0) {
                    continue;
                } else if (currentLine.startsWith("v ")) {
                    Vertex vertex = parseVertex(currentLine, lineCount);
                    if (vertex != null) {
                        vertices.add(vertex);
                    }
                } else if (currentLine.startsWith("vn ")) {
                    Vertex vertex = parseVertexNormal(currentLine, lineCount);
                    if (vertex != null) {
                        vertexNormals.add(vertex);
                    }
                } else if (currentLine.startsWith("vt ")) {
                    TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                    if (textureCoordinate != null) {
                        textureCoordinates.add(textureCoordinate);
                    }
                } else if (currentLine.startsWith("f ")) {

                    if (currentModelObject == null) {
                        currentModelObject = new ModelObject("Default");
                    }

                    Face face = parseFace(currentLine, lineCount);

                    if (face != null) {
                        currentModelObject.faces.add(face);
                    }
                } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                    ModelObject group = parseGroupObject(currentLine, lineCount);

                    if (group != null) {
                        if (currentModelObject != null) {
                            renderers.add(new ObjModelRenderer(model, currentModelObject));
                        }
                    }

                    currentModelObject = group;
                }
            }

            renderers.add(new ObjModelRenderer(model, currentModelObject));
        } catch (IOException e) {
            e.printStackTrace();
        }


        String[] path = modelFile.getAbsolutePath().split("/");
        String fileName = path[path.length - 1].split("\\.")[0];
        StringBuilder newPath = new StringBuilder();
        for (int i = 0; i < path.length - 1; i++) {
            if (i != 0) {
                newPath.append("/");
            }

            newPath.append(path[i]);
        }

        model.setParts(renderers);
        return model;
    }


    private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
        if (isValidVertexLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 2) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
                } else if (tokens.length == 3) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
                }
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Incorrect format");
        }

        return null;
    }

    private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {
        if (isValidVertexNormalLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 3)
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Incorrect format");
        }

        return null;
    }

    private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {
        if (isValidTextureCoordinateLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 2)
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
                else if (tokens.length == 3)
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Incorrect format");
        }

        return null;
    }

    private Face parseFace(String line, int lineCount) throws ModelFormatException {
        Face face;

        if (isValidFaceLine(line)) {
            face = new Face();

            String trimmedLine = line.substring(line.indexOf(" ") + 1);
            String[] tokens = trimmedLine.split(" ");
            String[] subTokens;

            if (tokens.length == 3) {
                if (currentModelObject.glDrawingMode == -1) {
                    currentModelObject.glDrawingMode = GL11.GL_TRIANGLES;
                } else if (currentModelObject.glDrawingMode != GL11.GL_TRIANGLES) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Invalid number of points for face (expected 4, found " + tokens.length + ")");
                }
            } else if (tokens.length == 4) {
                if (currentModelObject.glDrawingMode == -1) {
                    currentModelObject.glDrawingMode = GL11.GL_QUADS;
                } else if (currentModelObject.glDrawingMode != GL11.GL_QUADS) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Invalid number of points for face (expected 3, found " + tokens.length + ")");
                }
            }

            // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
            if (isValidFace_V_VT_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                    face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1/vt1 v2/vt2 v3/vt3 ...
            else if (isValidFace_V_VT_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1//vn1 v2//vn2 v3//vn3 ...
            else if (isValidFace_V_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("//");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1 v2 v3 ...
            else if (isValidFace_V_Line(line)) {
                face.vertices = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    face.vertices[i] = vertices.get(Integer.parseInt(tokens[i]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else {
                throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Incorrect format");
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Incorrect format");

        }

        return face;
    }

    private ModelObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
        ModelObject group = null;

        if (isValidGroupObjectLine(line)) {
            String trimmedLine = line.substring(line.indexOf(" ") + 1);

            if (trimmedLine.length() > 0) {
                group = new ModelObject(trimmedLine);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileLocation + "' - Incorrect format");
        }

        return group;
    }

    public File checkValidPath(String path) {
        File file = null;

        String absPath = path;

        if (!path.endsWith(".obj"))
            absPath += ".obj";

        file = new File(absPath);
        if (file == null || !file.exists())
            return null;
        return file;
    }
}