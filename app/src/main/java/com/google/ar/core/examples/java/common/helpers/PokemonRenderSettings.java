package com.google.ar.core.examples.java.common.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PokemonRenderSettings {

    public static final String SHARED_PREFERENCES_ID = "SHARED_PREFERENCES_MODEL";
    public static final String SHARED_PREFERENCES_MODEL_TO_RENDER = "model_to_render";

    // Current pokemon settings used by the app.
    private boolean depthColorVisualizationEnabled = false;
    private String pokemonMesh = "Charmander";
    private SharedPreferences sharedPreferences;

    /** Initializes the current settings based on when the app was last used. */
    public void onCreate(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
        pokemonMesh =
                sharedPreferences.getString(SHARED_PREFERENCES_MODEL_TO_RENDER, "Charmander");
    }

    public String meshToRender(){
        return pokemonMesh;
    }

    public void setPokemon(String pokemon){
        if(pokemonMesh.equals(pokemon)){
            return;
        }

        pokemonMesh = pokemon;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_MODEL_TO_RENDER, pokemonMesh);
        editor.apply();
    }
}
