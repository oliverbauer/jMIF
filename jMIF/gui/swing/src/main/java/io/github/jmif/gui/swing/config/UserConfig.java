package io.github.jmif.gui.swing.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * TODO Weld: Why: Exception in thread "AWT-EventQueue-0" org.jboss.weld.exceptions.IllegalStateException: WELD-000229: Contextual reference of Managed Bean [class io.github.jmif.gui.swing.config.UserConfig] with qualifiers [@Any @Default] is not valid after container 386b5c1e-554e-4ee1-8461-c9c4876cf5db shutdown
 *      on @ApplicationScoped ? 
 *      Without scope (@Dependent is default) this class gets instanciated three times (inefficient) 
 */
public class UserConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserConfig.class);

	private String GENERAL_PROFILE = "atsc_1080p_25";
	private boolean GENERAL_SHOW_SPLASHSCREEN = true;

	private int IMAGE_DURATION = 5000;
	private int IMAGE_OVERLAY = 1000;

	private int VIDEO_OVERLAY = 1000;

	private int AUDIO_FADE_IN = 1000;
	private int AUDIO_FADE_OUT = 1000;
	private boolean AUDIO_NORMALIZE = true;

	private int TEXT_DURATION = 5000;
	private String TEXT_BG = "0xff000080";
	private String TEXT_FG = "0x000000ff";
	private String TEXT_OL = "0x000000ff";

	public UserConfig() {
		LOGGER.info("New instance()...");

		String home = System.getProperty("user.home");
		if (new File(home + "/.jMIF/configuration.properties").exists()) {
			// load

			Configurations configs = new Configurations();
			try {
				Configuration config = configs.properties(new File(home + "/.jMIF/configuration.properties"));

				GENERAL_PROFILE = config.getString("GENERAL_PROFILE");
				GENERAL_SHOW_SPLASHSCREEN = config.getBoolean("GENERAL_SHOW_SPLASHSCREEN");
				
				IMAGE_DURATION = config.getInt("IMAGE_DURATION");
				IMAGE_OVERLAY = config.getInt("IMAGE_OVERLAY");
				
				VIDEO_OVERLAY = config.getInt("VIDEO_OVERLAY");
				
				AUDIO_FADE_IN = config.getInt("AUDIO_FADE_IN");
				AUDIO_FADE_OUT = config.getInt("AUDIO_FADE_OUT");
				AUDIO_NORMALIZE = config.getBoolean("AUDIO_NORMALIZE");
				
				TEXT_DURATION = config.getInt("TEXT_DURATION");
				TEXT_BG = config.getString("TEXT_BG");
				TEXT_FG = config.getString("TEXT_FG");
				TEXT_OL = config.getString("TEXT_OL");
				LoggerFactory.getLogger(getClass()).info("Config successfully loaded from HOME-DIR");
			} catch (ConfigurationException cex) {
				LoggerFactory.getLogger(getClass()).error("Unable to read config from HOME-DIR", cex);
			}

		} else {

			
			Configurations configs = new Configurations();
			try {
				new File(home + "/.jMIF").mkdirs();
				new File(home + "/.jMIF/configuration.properties").createNewFile();
				
				FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(new File(home + "/.jMIF/configuration.properties"));
				Configuration config = builder.getConfiguration();

				// update property
				config.addProperty("GENERAL_PROFILE", GENERAL_PROFILE);
				config.addProperty("GENERAL_SHOW_SPLASHSCREEN", GENERAL_SHOW_SPLASHSCREEN);
				
				config.addProperty("IMAGE_DURATION", IMAGE_DURATION);
				config.addProperty("IMAGE_OVERLAY", IMAGE_OVERLAY);
				
				config.addProperty("VIDEO_OVERLAY", VIDEO_OVERLAY);
				
				config.addProperty("AUDIO_FADE_IN", AUDIO_FADE_IN);
				config.addProperty("AUDIO_FADE_OUT", AUDIO_FADE_OUT);
				config.addProperty("AUDIO_NORMALIZE", AUDIO_NORMALIZE);
				
				config.addProperty("TEXT_DURATION", TEXT_DURATION);
				config.addProperty("TEXT_BG", TEXT_BG);
				config.addProperty("TEXT_FG", TEXT_FG);
				config.addProperty("TEXT_OL", TEXT_OL);

				// save configuration
				builder.save();
				
				LoggerFactory.getLogger(getClass()).info("Config successfully stored in HOME-DIR");
			} catch (ConfigurationException | IOException cex) {
				LoggerFactory.getLogger(getClass()).error("Unable to store config in HOME-DIR", cex);
			}
		}
	}

	public String getGENERAL_PROFILE() {
		return GENERAL_PROFILE;
	}

	public void setGENERAL_PROFILE(String gENERAL_PROFILE) {
		GENERAL_PROFILE = gENERAL_PROFILE;
	}

	public boolean isGENERAL_SHOW_SPLASHSCREEN() {
		return GENERAL_SHOW_SPLASHSCREEN;
	}

	public void setGENERAL_SHOW_SPLASHSCREEN(boolean gENERAL_SHOW_SPLASHSCREEN) {
		GENERAL_SHOW_SPLASHSCREEN = gENERAL_SHOW_SPLASHSCREEN;
	}

	public int getIMAGE_DURATION() {
		return IMAGE_DURATION;
	}

	public void setIMAGE_DURATION(int iMAGE_DURATION) {
		IMAGE_DURATION = iMAGE_DURATION;
	}

	public int getIMAGE_OVERLAY() {
		return IMAGE_OVERLAY;
	}

	public void setIMAGE_OVERLAY(int iMAGE_OVERLAY) {
		IMAGE_OVERLAY = iMAGE_OVERLAY;
	}

	public int getVIDEO_OVERLAY() {
		return VIDEO_OVERLAY;
	}

	public void setVIDEO_OVERLAY(int vIDEO_OVERLAY) {
		VIDEO_OVERLAY = vIDEO_OVERLAY;
	}

	public int getAUDIO_FADE_IN() {
		return AUDIO_FADE_IN;
	}

	public void setAUDIO_FADE_IN(int aUDIO_FADE_IN) {
		AUDIO_FADE_IN = aUDIO_FADE_IN;
	}

	public int getAUDIO_FADE_OUT() {
		return AUDIO_FADE_OUT;
	}

	public void setAUDIO_FADE_OUT(int aUDIO_FADE_OUT) {
		AUDIO_FADE_OUT = aUDIO_FADE_OUT;
	}

	public boolean isAUDIO_NORMALIZE() {
		return AUDIO_NORMALIZE;
	}

	public void setAUDIO_NORMALIZE(boolean aUDIO_NORMALIZE) {
		AUDIO_NORMALIZE = aUDIO_NORMALIZE;
	}

	public int getTEXT_DURATION() {
		return TEXT_DURATION;
	}

	public void setTEXT_DURATION(int tEXT_DURATION) {
		TEXT_DURATION = tEXT_DURATION;
	}

	public String getTEXT_BG() {
		return TEXT_BG;
	}

	public void setTEXT_BG(String tEXT_BG) {
		TEXT_BG = tEXT_BG;
	}

	public String getTEXT_FG() {
		return TEXT_FG;
	}

	public void setTEXT_FG(String tEXT_FG) {
		TEXT_FG = tEXT_FG;
	}

	public String getTEXT_OL() {
		return TEXT_OL;
	}

	public void setTEXT_OL(String tEXT_OL) {
		TEXT_OL = tEXT_OL;
	}
}
