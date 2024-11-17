package net.coolstop.betterintro;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BetterIntro extends Screen implements ClientModInitializer {

	public static final String MOD_ID = "betterintro";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final List<Identifier> frameTextures;
	private int currentFrame = 0;
	private long lastFrameTime;
	private static final int FRAME_DURATION_MS = 50; // 20 fps
	private final int totalFrames;
	private float progress = 0.0f;

	// Singleton pattern
	private static BetterIntro instance;
	public static BetterIntro getInstance() { return instance; }
	public static void setInstance(BetterIntro screen) { instance = screen; }

	public BetterIntro() {
		super(Text.literal("Loading..."));
		frameTextures = new ArrayList<>();

		// Log the start of loading screen creation
		LOGGER.info("Creating BetterIntro...");

		// Load all frame textures
		int frameCount = 1;
		while (true) {
			String frameNumber = String.format("%03d", frameCount);
			String texturePath = "textures/loading_video/frames/ezgif-frame-" + frameNumber + ".png";
			Identifier frameId = Identifier.of(MOD_ID, texturePath);

			// Try to get the resource, break if not found
			if (client != null && client.getResourceManager().getResource(frameId).isEmpty()) {
				LOGGER.info("Loaded {} frames in total.", frameCount - 1);
				break;
			}

			frameTextures.add(frameId);
			frameCount++;
		}

		totalFrames = frameTextures.size();
		lastFrameTime = System.currentTimeMillis();

		// Set this instance as the current one
		setInstance(this);

		// Log the number of frames loaded
		LOGGER.info("Loaded {} frames.", totalFrames);
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("!!!!!!!!!!!!!!!!! BetterIntro Mod is starting");

		// Set the loading screen when the mod is initialized
		client.setScreen(new BetterIntro());
	}

	public void setProgress(float progress) {
		this.progress = progress;
		LOGGER.debug("Setting progress to: {:.2f}%", progress * 100);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (client == null) return;

		// Fill background with black
		context.fill(0, 0, this.width, this.height, 0xFF000000);

		// Update animation frame
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastFrameTime >= FRAME_DURATION_MS) {
			currentFrame = (currentFrame + 1) % totalFrames;
			lastFrameTime = currentTime;
		}

		// Render current frame
		if (!frameTextures.isEmpty()) {
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, frameTextures.get(currentFrame));

			// Draw the frame scaled to fit the screen while maintaining aspect ratio
			float aspectRatio = 16.0f / 9.0f;
			int drawWidth = width;
			int drawHeight = (int)(width / aspectRatio);

			if (drawHeight > height) {
				drawHeight = height;
				drawWidth = (int)(height * aspectRatio);
			}

			int x = (width - drawWidth) / 2;
			int y = (height - drawHeight) / 2;

			context.drawTexture(frameTextures.get(currentFrame), x, y, 0, 0, drawWidth, drawHeight, drawWidth, drawHeight);
		}

		// Draw progress bar
		int progressBarWidth = width * 3 / 4;
		int progressBarHeight = 10;
		int progressBarX = (width - progressBarWidth) / 2;
		int progressBarY = height * 3 / 4;

		// Draw progress bar background
		context.fill(progressBarX - 1, progressBarY - 1,
				progressBarX + progressBarWidth + 1, progressBarY + progressBarHeight + 1,
				0xFF000000);

		// Draw progress bar fill
		int fillWidth = (int)(progressBarWidth * progress);
		context.fill(progressBarX, progressBarY,
				progressBarX + fillWidth, progressBarY + progressBarHeight,
				0xFF00FF00);

		// Log progress
		LOGGER.debug("Progress: {:.2f}%", progress * 100);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean shouldPause() {
		return true;
	}
}
