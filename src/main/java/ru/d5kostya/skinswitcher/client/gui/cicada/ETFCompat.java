package ru.d5kostya.skinswitcher.client.gui.cicada;

import traben.entity_texture_features.features.ETFRenderContext;

public class ETFCompat {
    public static void preventRenderLayerIssue() {
        ETFRenderContext.preventRenderLayerTextureModify();
    }
}
