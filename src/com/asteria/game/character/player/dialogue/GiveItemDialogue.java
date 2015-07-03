package com.asteria.game.character.player.dialogue;

import com.asteria.game.item.Item;

/**
 * The dialogue chain entry that gives the player an item.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GiveItemDialogue implements DialogueChain {

    /**
     * The item to give to the player during this chain.
     */
    private final Item item;

    /**
     * The text to display when the item is given.
     */
    private final String text;

    /**
     * Creates a new {@link GiveItemDialogue}.
     *
     * @param item
     *            the item to give to the player during this chain.
     * @param text
     *            the text to display when the item is given.
     */
    public GiveItemDialogue(Item item, String text) {
        this.item = item;
        this.text = text;
    }

    @Override
    public void accept(DialogueChainBuilder dialogue) {
        if (dialogue.getPlayer().getInventory().add(item)) {
            dialogue.getPlayer().getMessages().sendString(text, 308);
            dialogue.getPlayer().getMessages().sendItemModelOnInterface(307, 200, item.getId());
            dialogue.getPlayer().getMessages().sendChatInterface(306);
        } else {
            dialogue.getPlayer().getMessages().sendChatboxString("You do not " + "have enough space in your inventory!");
            dialogue.interrupt();
        }
    }
}