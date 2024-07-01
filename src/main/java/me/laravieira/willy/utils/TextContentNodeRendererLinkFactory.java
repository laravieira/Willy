package me.laravieira.willy.utils;

import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;

public class TextContentNodeRendererLinkFactory implements TextContentNodeRendererFactory {

    @Override
    public NodeRenderer create(TextContentNodeRendererContext context) {
        return new MarkdownTextLinkRenderer(context);
    }
}
