// Made with Blockbench 4.12.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class mario_fireball extends EntityModel<Entity> {
	private final ModelPart fireball;
	public mario_fireball(ModelPart root) {
		this.fireball = root.getChild("fireball");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData fireball = modelPartData.addChild("fireball", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 21.0F, 0.0F));

		ModelPartData tail4_r1 = fireball.addChild("tail4_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, 3.0F, -1.3963F, 0.0F, 0.0F));

		ModelPartData tail3_r1 = fireball.addChild("tail3_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 3.0F, 3.0F, -2.9671F, 0.0F, 0.0F));

		ModelPartData tail2_r1 = fireball.addChild("tail2_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 3.0F, -3.0F, 1.7453F, 0.0F, 0.0F));

		ModelPartData tail1_r1 = fireball.addChild("tail1_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, -3.0F, 0.1745F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 24, 18);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		fireball.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}