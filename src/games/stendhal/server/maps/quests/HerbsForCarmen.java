/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Herbs For Carmen
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Carmen (the healer in Semos)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Carmen introduces herself and asks for some items to help her heal people.</li>
 * <li>You collect the items.</li>
 * <li>Carmen sees yours items, asks for them then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>2 antidote</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class HerbsForCarmen extends AbstractQuest {

	public static final String QUEST_SLOT = "herbs_for_carmen";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "arandula=5;porcini=1;apple=3;wood=2;button mushroom=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Carmen 向我索要冶病配方，以便可以治疗其他病人。");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我不想帮助 Carmen. 我猜她可以找别人帮助。");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("我还需要把 " + missingItems.toStringList() + "带给 Carmen.");
		} else {
			res.add("我帮助了 Carmen ，现在她可以继续治病救人了。");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Carmen");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1,
			"Hm, 你知道我为何而生吗?", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED,
			"嗨, 你还要帮我吗？?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"太好了, 你知道了我的工作，我用于治病的 #ingredients 配方原料不足。",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我是 Carmen. 我能免费为你治病，直到你变得更强之前。很多勇者都向我寻求帮助。但现在我的 #ingredients 配方不够了，我需要补充我的药材",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"ingredients",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"所以很多人需要我的治疗。但那需要很多药材，现在我的存货几乎空了，你能帮我把原料补足吗？",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Oh 太好了. 请带给我这些药材: [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"哈啊, 不行! 不过好吧，这是你的选择，但记住，我会对其他人说因为你没有帮我，我将不能继续治疗他们。",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.ATTENDING,
			"apple",
			null,
			ConversationStates.ATTENDING,
			"苹果有很多维生素，我曾看到一些苹果树在 Semon 镇的东边。",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"wood",
			null,
			ConversationStates.ATTENDING,
			"木头是个好东西，可以用在不同的地方，当然你可以在森林中找到一些。",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("button mushroom","porcino","porcini","porcinis"),
			null,
			ConversationStates.ATTENDING,
			"有人告诉我在 Semos 森林中有一些不同种类的蘑菇，从这往南走就能到。",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"arandula",
			null,
			ConversationStates.ATTENDING,
			"出了 Semos 向北, 在 grove 树的旁边，生着名叫 arnandula 的药草，给你看看它的图片，方但你找到它。",
			new ExamineChatAction("arandula.png", "Carmen's drawing", "Arandula"));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Carmen");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"又见面了，我可以为你 #heal 治疗, 但如果你带给我 #ingredients 药材我会更开心的给你治疗!",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "ingredients", null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我需要 [items]. 你能带一些过来吗?"));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2,
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我需要 [items]. 你能带过来一些吗?"));

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "太好了, 我看看你带来了什么?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("太好了! 现在我能免费治好很多人了，非常感谢。你工作时可以带上这个。"),
				new IncreaseXPAction(50),
				new IncreaseKarmaAction(5),
				new EquipItemAction("minor potion", 5)
				);

		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> entry : items.entrySet()) {
			String itemName = entry.getKey();

			String singular = itemName;
			List<String> sl = new ArrayList<String>();
			sl.add(itemName);

			// handle the porcino/porcini singular/plural case with item name "porcini"
			if (!singular.equals(itemName)) {
				sl.add(singular);
			}
			// also allow to understand the misspelled "porcinis"
			if (itemName.equals("porcini")) {
				sl.add("porcinis");
			}

			npc.add(ConversationStates.QUESTION_2, sl, null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							"好的, 你还有其他事吗?", "你还为我带回来了 " +
								entry.getValue()+ itemName + " ,谢谢。",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Ok, 如果我能为你做点什么，就告诉我 #help 。",
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Ok, 如果我能幚到你 #Help 的，一定告诉我。", null);

		/* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING,
			    "现在没有我需要的东西了，谢谢你。",
			    null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Herbs for Carmen",
				"Semos 的医者, Carmen 为了做出足够的药济，正到处寻找药济的原料.",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "HerbsForCarmen";
	}

	public String getTitle() {

		return "Herbs for Carmen";
	}

	@Override
	public int getMinLevel() {
		return 3;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Carmen";
	}
}
