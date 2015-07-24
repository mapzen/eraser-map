package com.mapzen.erasermap.model;

public class ManifestModel {
    private double minVersion;
    private String vectorTileApiKeyReleaseProp;
    private String valhallaApiKey;
    private String mintApiKey;
    private String peliasApiKey;

    public double getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(double minVersion) {
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

    public String getMintApiKey() {
        return mintApiKey;
    }

    public void setMintApiKey(String mintApiKey) {
        this.mintApiKey = mintApiKey;
    }

    public String getPeliasApiKey() {
        return peliasApiKey;
    }

    public void setPeliasApiKey(String peliasApiKey) {
        this.peliasApiKey = peliasApiKey;
    }
}
