package com.europa.rpg.client;

import com.europa.rpg.entity.CatacombLordEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/** Reaproveita o modelo do esqueleto wither; a escala vem do atributo GENERIC_SCALE da entidade. */
public class CatacombLordRenderer extends SkeletonEntityRenderer<CatacombLordEntity> {
    private static final Identifier TEXTURE = Identifier.of("europa", "textures/entity/catacomb_lord.png");

    public CatacombLordRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, EntityModelLayers.WITHER_SKELETON, EntityModelLayers.WITHER_SKELETON_INNER_ARMOR, EntityModelLayers.WITHER_SKELETON_OUTER_ARMOR);
    }

    @Override
    public Identifier getTexture(CatacombLordEntity entity) {
        return TEXTURE;
    }
}
