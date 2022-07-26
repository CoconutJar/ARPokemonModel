package com.google.ar.core.examples.java.common.helpers;

import com.google.ar.core.examples.java.common.samplerender.Mesh;
import com.google.ar.core.examples.java.common.samplerender.Shader;
import com.google.ar.core.Anchor;

public class Pokemon {
    private Mesh mesh;
    private Shader shader;
    private String name;
    public Anchor anchor;

    public Pokemon(Mesh m, Shader s, String n, Anchor a){
        mesh = m;
        shader = s;
        name = "Pokemon";
        anchor = a;
    }

    public Mesh getMesh(){
        return mesh;
    }

    public Shader getShader(){
        return shader;
    }

    public Anchor getAnchor(){
        return anchor;
    }

    public void setMesh(Mesh m){
        mesh = m;
    }

    public void setShader(Shader s){
        shader = s;
    }

    public void setName(String n) { name = n; }
}
