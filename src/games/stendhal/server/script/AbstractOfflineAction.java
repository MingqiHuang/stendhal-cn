package games.stendhal.server.script;

import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

import org.apache.log4j.Logger;

/**
 * Changes the attributes of an offline player.
 *
 * @author hendrik
 */
public abstract class AbstractOfflineAction extends ScriptImpl {
	private static Logger logger = Logger.getLogger(AbstractOfflineAction.class);

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		// validate and read parameters
		if (!validateParameters(admin, args)) {
			return;
		}
		String playerName = args.get(0);

		// check that player is offline
		if (StendhalRPRuleProcessor.get().getPlayer(playerName) != null) {
			admin.sendPrivateText("This player is currently online. Please use the normal commands.");
			return;
		}

		// start a transaction
		CharacterDAO characterDAO = DAORegister.get().get(CharacterDAO.class);
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {

			// check that the player exists
			if (!characterDAO.hasCharacter(playerName, playerName)) {
				admin.sendPrivateText("No player with that name.");
				TransactionPool.get().commit(transaction);
				return;
			}

			RPObject object = characterDAO.loadCharacter(transaction, playerName, playerName);

			process(admin, object, args);

			// safe it back
			characterDAO.storeCharacter(transaction, playerName, playerName, object);
			TransactionPool.get().commit(transaction);

			// remove from world
			IRPZone zone = StendhalRPWorld.get().getRPZone(object.getID());
			if (zone != null) {
				zone.remove(object.getID());
			}

		} catch (Exception e) {
			logger.error(e, e);
			admin.sendPrivateText(e.toString());
			TransactionPool.get().rollback(transaction);
		}
	}

	/**
	 * validates the parameters, sends an error message, if something is wrong with them
	 *
	 * @param admin admin executing the script
	 * @param args arguments for the script
	 * @return true if the parameters are valid, false otherwise
	 */
	public abstract boolean validateParameters(final Player admin, final List<String> args);

	/**
	 * processes the requested operation on the loaded object
	 *
	 * @param admin admin executing the script
	 * @param object the RPObject of the player loaded from the database
	 * @param args arguments for the script
	 */
	public abstract void process(final Player admin, RPObject object, final List<String> args);
}
