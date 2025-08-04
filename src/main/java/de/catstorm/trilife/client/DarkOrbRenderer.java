package de.catstorm.trilife.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class DarkOrbRenderer extends EntityRenderer<AbstractWindChargeEntity> {
    private static final float field_52258 = MathHelper.square(3.5F);
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/dark_orb.png");
    private final WindChargeEntityModel model;

    public DarkOrbRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new WindChargeEntityModel(context.getPart(EntityModelLayers.WIND_CHARGE));
    }

    public void render(AbstractWindChargeEntity abstractWindChargeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (abstractWindChargeEntity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(abstractWindChargeEntity) < (double)field_52258)) {
            float h = (float)abstractWindChargeEntity.age + g;
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(h) % 1.0F, 0.0F));
            this.model.setAngles(abstractWindChargeEntity, 0.0F, 0.0F, h, 0.0F, 0.0F);
            this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
            super.render(abstractWindChargeEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    protected float getXOffset(float tickDelta) {
        return tickDelta * 0.03F;
    }

    public Identifier getTexture(AbstractWindChargeEntity abstractWindChargeEntity) {
        return TEXTURE;
    }
}