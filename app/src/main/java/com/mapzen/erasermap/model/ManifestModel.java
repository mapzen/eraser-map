package com.mapzen.erasermap.model;

public class ManifestModel {
    private int minVersion;
    private String vectorTileApiKeyReleaseProp;
    private String valhallaApiKey;
    private String peliasApiKey;

    public int getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(int minVersion) {
        this.minVersion = minVersion;
    }

    public String getVectorTileApiKeyReleaseProp() {
        return vectorTileApiKeyReleaseProp;
    }

    public void setVectorTileApiKeyReleaseProp(String vectorTileApiKeyReleaseProp) {
        this.vectorTileApiKeyReleaseProp = vectorTileApiKeyReleaseProp;
    }

    public String getValhallaApiKey() {
        return valhallaApiKey;
    }

    public void setValhallaApiKey(String valhallaApiKey) {
        this.valhallaApiKey = valhallaApiKey;
    }

    public String getPeliasApiKey() {
        return peliasApiKey;
    }

    public void setPeliasApiKey(String peliasApiKey) {
        this.peliasApiKey = peliasApiKey;
    }
}
