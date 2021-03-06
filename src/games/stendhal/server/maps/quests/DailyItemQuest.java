/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Daily Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor of Ados
 * <li> some items
 * <p>
 * STEPS:
 * <li> talk to Mayor of Ados to get a quest to fetch an item
 * <li> bring the item to the mayor
 * <li> if you cannot bring it in one week he offers you the chance to fetch
 * another instead
 * <p>
 * REWARD:
 * <li> xp
 * <li> 10 Karma
 * <p>
 * REPETITIONS:
 * <li> once a day
 */
public class DailyItemQuest extends AbstractQuest {

	private static final String QUEST_SLOT = "daily_item";

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK;

	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_DAY;

	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();

		// ammunition
		items.put("golden arrow",5);
		items.put("power arrow",5);
		items.put("steel arrow",7);
		items.put("wooden arrow",10);

		// armor
        items.put("blue armor",1);
		items.put("chain armor",1);
		items.put("dress",1);
		items.put("enhanced chainmail",1);
		items.put("golden chainmail",1);
		items.put("iron scale armor",1);
		items.put("leather armor",1);
		items.put("leather cuirass",1);
		items.put("leather scale armor",1);
		items.put("pauldroned iron cuirass",1);
		items.put("pauldroned leather cuirass",1);
		items.put("plate armor",1);
		items.put("scale armor",1);
		items.put("studded armor",1);

		// axe
		items.put("axe",1);
		items.put("bardiche",1);
		items.put("battle axe",1);
		items.put("halberd",1);
		items.put("hand axe",1);
		items.put("old scythe",1);
		items.put("scythe",1);
		items.put("sickle",1);
		items.put("small axe",1);
		items.put("twoside axe",1);

		// boots
        items.put("blue boots",1);
		items.put("chain boots",1);
		items.put("leather boots",1);
		items.put("studded boots",1);

		// cloaks
		items.put("blue elf cloak",1);
		items.put("cloak",1);
		items.put("dwarf cloak",1);
		items.put("elf cloak",1);
		items.put("green dragon cloak",1);

		// club
		items.put("club",1);
		items.put("enhanced mace",1);
		items.put("flail",1);
		items.put("golden hammer",1);
		items.put("golden mace",1);
		items.put("hammer",1);
		items.put("mace",1);
		items.put("morning star",1);
		items.put("staff",1);
		items.put("war hammer",1);

		// container
		items.put("eared bottle",3);
		items.put("flask",5);
		items.put("slim bottle",5);

		// drinks
		items.put("antidote",5);
		items.put("beer",10);
		items.put("greater antidote",5);
		items.put("greater potion",5);
		items.put("milk",5);
		items.put("minor potion",5);
		items.put("poison",5);
		items.put("potion",5);
		items.put("tea",3);
		items.put("water",5);
		items.put("wine",10);

		// flower
		items.put("daisies",5);
		items.put("lilia",5);
		items.put("rose",10);

		// food
		items.put("apple",5);
		items.put("apple pie",2);
		items.put("artichoke",10);
		items.put("bread",5);
		items.put("broccoli",5);
		items.put("button mushroom",10);
		items.put("carrot",10);
		items.put("cauliflower",5);
		items.put("char",5);
		items.put("cheese",10);
		items.put("cherry",10);
		items.put("cherry pie",2);
		items.put("chicken",10);
		items.put("chocolate bar",5);
		items.put("clownfish",5);
		items.put("collard",5);
		items.put("courgette",5);
		items.put("egg",1);
		items.put("fairy cake", 5);
		items.put("fish pie",1);
		items.put("garlic",5);
		items.put("grapes",5);
		items.put("grilled steak",1);
		items.put("ham",10);
		items.put("honey",2);
		items.put("leek",5);
		items.put("meat",10);
		items.put("onion",5);
		items.put("pear",5);
		items.put("perch",5);
		items.put("pie",5);
		items.put("pomegranate",5);
		items.put("porcini",10);
		items.put("red lionfish",5);
		items.put("roach",5);
		items.put("salad",10);
		items.put("sandwich",5);
		items.put("spinach",5);
		items.put("surgeonfish",5);
		items.put("toadstool",15);
		items.put("tomato",5);
		items.put("trout",5);
		items.put("olive",5);

		// helmet
        items.put("blue helmet",1);
		items.put("aventail",1);
		items.put("chain helmet",1);
		items.put("leather helmet",1);
		items.put("robins hat",1);
		items.put("studded helmet",1);
		items.put("viking helmet",1);

		// herb
		items.put("arandula",5);
		items.put("sclaria",5);
		items.put("mandragora",3);
		items.put("kekik",5);

		// legs
        items.put("blue legs",1);
		items.put("chain legs",1);
		items.put("leather legs",1);
		items.put("studded legs",1);

		// misc
		items.put("dice",1);
		items.put("marbles", 2);
		items.put("rodent trap",5);
		items.put("teddy",1);

		// money
		items.put("money",100);

		// ranged
		items.put("composite bow",1);
		items.put("longbow",1);
		items.put("wooden bow",1);

		// resource
		items.put("coal",10);
		items.put("flour",5);
		items.put("grain",20);
		items.put("gold bar",5);
		items.put("gold nugget",10);
		items.put("iron",5);
		items.put("iron ore",10);
		items.put("wood",10);

		// shield
        items.put("blue shield",1);
		items.put("buckler",1);
		items.put("crown shield",1);
		items.put("enhanced lion shield",1);
		items.put("lion shield",1);
		items.put("plate shield",1);
		items.put("skull shield",1);
		items.put("studded shield",1);
		items.put("unicorn shield",1);
		items.put("wooden shield",1);

		// sword
		items.put("biting sword",1);
		items.put("broadsword",1);
		items.put("claymore",1);
		items.put("dagger",1);
		items.put("elvish sword",1);
		items.put("katana",1);
		items.put("knife",1);
		items.put("scimitar",1);
		items.put("short sword",1);
		items.put("sword",1);

		// tool
		items.put("pick",1);
		items.put("gold pan",1);
	}

	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Ados 城需根支援，快去找 [item] 物资"
				+ " ,当你找到了，回来说 #complete 完成任务"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		return new MultipleActions(actions);
	}

	private void getQuest() {
		final SpeakerNPC npc = npcs.get("Mayor Chalmers");
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"你已经有了一个取得 [item] 物资的任务。"
						+ ". 如果你能回来，回复 #complete 可完成!"));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"你已有了一个取得 [item] 物资的任务。"
						+ ". 如果你把物资带回来，对我说 #complete 完成它。可能你根本没有那种物资剩下，你也可以拿回 #another 物资，或者带回我向你要的物资。"));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "我一天只能交给你一次任务，请核对任务记录。"));




		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				startQuestAction());
	}

	private void completeQuest() {
		final SpeakerNPC npc = npcs.get("Mayor Chalmers");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"恐怕我还没有交给你任何 #quest 任务。",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"你已完成我交给你的全部任务。",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(8, 90.0));
		actions.add(new IncreaseKarmaAction(10.0));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING,
				"干的好! 我代表Ados城的人民对你表示感谢。",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"你仍没有拿到 [item]"
						+ " 。当你拿到后，回去只用说 #complete 就能完成任务了。"));

	}

	private void abortQuest() {
		final SpeakerNPC npc = npcs.get("Mayor Chalmers");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				// start quest again immediately
				startQuestAction());

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				"你已开始做这个任务很长了，我不让你这么快放弃。",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new QuestNotActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"恐怕我还没有给你发过一个 #quest 请求.",
				null);

	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我在Ados城镇大厅中会见了城主 Chalmers。");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我不想帮助Ados城。");
			return res;
		}

		res.add("我想帮助Ados城。");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add("要帮助Ados城，需要取得一个"
						+  questItem  + " ，但我身上还没有。");
			} else {
				res.add("我找到了"
						+ questItem + " ，这下可以回去帮助Ados城了。");
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("目前，我已向Ados城提供了 "
					+ repetitions+ "次帮助。");
		}
		if (isRepeatable(player)) {
			res.add("我取来城主让我去寻找的最后一件东西，Ados城又有了新需要。");
		} else if (isCompleted(player)){
			res.add("我取来城主让我去寻找的最后一件东西，并要求我在24小时内送到。");
		}
		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"每日物品任务",
				"城主Chalmers需要为Ados城供应各类物资。",
				true);

		buildItemsMap();

		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "DailyItemQuest";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Mayor Chalmers";
	}
}
