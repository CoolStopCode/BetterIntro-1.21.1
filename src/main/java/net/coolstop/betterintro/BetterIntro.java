package net.coolstop.betterintro;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
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
	private static final int FRAME_DURATION_MS = 50;
	private float progress = 0.0f;
	private boolean initialized = false;

	private static BetterIntro instance;

	public static BetterIntro getInstance() {
		if (instance == null) {
			instance = new BetterIntro();
		}
		return instance;
	}

	public BetterIntro() {
		super(Text.literal("Loading..."));
		frameTextures = new ArrayList<>();
		lastFrameTime = System.currentTimeMillis();
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("BetterIntro initialized");
	}

	@Override
	protected void init() {
		if (!initialized) {
			initializeFrames();
			initialized = true;
		}
	}

	private void initializeFrames() {
		LOGGER.info("Loading frames for BetterIntro...");
		frameTextures.clear();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.getResourceManager() == null) {
			LOGGER.error("Client or ResourceManager is null!");
			return;
		}

		// Load frames from 1 to 1000 (or however many you have)
		for (int i = 1; i <= 56; i++) {
			String frameNumber = String.format("%03d", i);
			// Using Identifier.of() instead of the constructor
			Identifier frameId = Identifier.of(MOD_ID, String.format("textures/gui/loading/frame_%s.png", frameNumber));

			if (client.getResourceManager().getResource(frameId).isPresent()) {
				frameTextures.add(frameId);
				LOGGER.debug("Loaded frame {}", frameNumber);
			} else {
				LOGGER.info("Reached end of frames at {}", frameNumber);
				break;
			}
		}

		LOGGER.info("Successfully loaded {} frames", frameTextures.size());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (client == null || frameTextures.isEmpty()) {
			return;
		}

		// Fill background with black
		context.fill(0, 0, this.width, this.height, 0xFF000000);

		// Update frame
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastFrameTime >= FRAME_DURATION_MS) {
			currentFrame = (currentFrame + 1) % frameTextures.size();
			lastFrameTime = currentTime;
		}

		// Render current frame
		try {
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, frameTextures.get(currentFrame));

			// Calculate dimensions maintaining aspect ratio
			float aspectRatio = 16.0f / 9.0f;
			int drawWidth = width;
			int drawHeight = (int)(width / aspectRatio);

			if (drawHeight > height) {
				drawHeight = height;
				drawWidth = (int)(height * aspectRatio);
			}

			int x = (width - drawWidth) / 2;
			int y = (height - drawHeight) / 2;

			// Draw the frame
			context.drawTexture(frameTextures.get(currentFrame), x, y, 0, 0, drawWidth, drawHeight, drawWidth, drawHeight);

			// Draw progress bar
			renderProgressBar(context);
		} catch (Exception e) {
			LOGGER.error("Error rendering frame: ", e);
		}
	}

	private void renderProgressBar(DrawContext context) {
		int progressBarWidth = width * 3 / 4;
		int progressBarHeight = 10;
		int progressBarX = (width - progressBarWidth) / 2;
		int progressBarY = height * 3 / 4;

		// Background
		context.fill(progressBarX - 1, progressBarY - 1,
				progressBarX + progressBarWidth + 1, progressBarY + progressBarHeight + 1,
				0xFF000000);

		// Progress fill
		int fillWidth = (int)(progressBarWidth * progress);
		context.fill(progressBarX, progressBarY,
				progressBarX + fillWidth, progressBarY + progressBarHeight,
				0xFF00FF00);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean shouldPause() {
		return true;
	}

	public void setProgress(float newProgress) {
		this.progress = newProgress;

		if (newProgress >= 1.0f && client != null && client.currentScreen instanceof BetterIntro) {
			client.setScreen(null);
			instance = null;
		}
	}
}