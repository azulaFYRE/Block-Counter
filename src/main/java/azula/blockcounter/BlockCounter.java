package azula.blockcounter;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockCounter implements ModInitializer {
	public static final String MOD_ID = "block-counter";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Block Counter initialized");
	}
}