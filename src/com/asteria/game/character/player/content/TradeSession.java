package com.asteria.game.character.player.content;

import com.asteria.game.character.player.IOState;
import com.asteria.game.character.player.Player;
import com.asteria.game.character.player.Rights;
import com.asteria.game.item.Item;
import com.asteria.game.item.container.ItemContainer;
import com.asteria.game.item.container.ItemContainerPolicy;

/**
 * The container class that represents a trade session between players.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class TradeSession {

    /**
     * The player that controls this trade session.
     */
    private final Player player;

    /**
     * The container of items in this trade session.
     */
    private final ItemContainer container = new ItemContainer(28, ItemContainerPolicy.NORMAL);

    /**
     * The other player in this trade session.
     */
    private Player other;

    /**
     * The stage that this trade session is in.
     */
    private TradeStage stage;

    /**
     * Creates a new {@link TradeSession}.
     *
     * @param player
     *            the player that controls this trade session.
     */
    public TradeSession(Player player) {
        this.player = player;
    }

    /**
     * Attempts to add {@code item} from {@code inventorySlot} onto the trade
     * offer screen.
     *
     * @param item
     *            the item to attempt to add to the trade.
     * @param inventorySlot
     *            the inventory slot that the item is located on.
     */
    public void add(Item item, int inventorySlot) {
        if (!inTradeSession())
            return;
        if (!Item.valid(item) || !player.getInventory().contains(item.getId()))
            return;
        if (!item.getDefinition().isTradeable()) {
            player.getMessages().sendMessage("You cannot trade this item!");
            return;
        }
        if (item.getAmount() > player.getInventory().amount(item.getId()) && !item.getDefinition().isStackable()) {
            item.setAmount(player.getInventory().amount(item.getId()));
        } else if (item.getAmount() > player.getInventory().get(inventorySlot).getAmount() && item.getDefinition().isStackable()) {
            item.setAmount(player.getInventory().get(inventorySlot).getAmount());
        }
        if (container.add(item)) {
            player.getInventory().remove(item, inventorySlot);
            String trade = name(player);
            int remaining = player.getInventory().remaining();
            other.getMessages().sendString("Trading with: " + trade + " who " + "has @gre@" + remaining + " free slots", 3417);
            player.getMessages().sendItemsOnInterface(3322, player.getInventory().container());
            int length = container.size();
            player.getMessages().sendItemsOnInterface(3415, container.container(), length);
            other.getMessages().sendItemsOnInterface(3416, container.container(), length);
            stage = TradeStage.OFFER;
            other.getTradeSession().setStage(TradeStage.OFFER);
            player.getMessages().sendString("", 3431);
            other.getMessages().sendString("", 3431);
        }
    }

    /**
     * Attempts to remove {@code item} from the trade offer screen.
     *
     * @param item
     *            the item to attempt to remove from the trade.
     */
    public void remove(Item item) {
        if (!inTradeSession())
            return;
        if (!container.contains(item.getId()))
            return;
        if (item.getAmount() > container.amount(item.getId()))
            item.setAmount(container.amount(item.getId()));
        if (container.remove(item)) {
            player.getInventory().add(item);
            String trade = name(player);
            int remaining = player.getInventory().remaining();
            other.getMessages().sendString("Trading with: " + trade + " who " + "has @gre@" + remaining + " free slots", 3417);
            player.getMessages().sendItemsOnInterface(3322, player.getInventory().container());
            int length = container.size();
            player.getMessages().sendItemsOnInterface(3415, container.container(), length);
            other.getMessages().sendItemsOnInterface(3416, container.container(), length);
            stage = TradeStage.OFFER;
            other.getTradeSession().stage = TradeStage.OFFER;
            player.getMessages().sendString("", 3431);
            other.getMessages().sendString("", 3431);
        }
    }

    /**
     * Attempts to initiate this trade session with {@code requester}.
     *
     * @param other
     *            the person requesting or receiving a request for a trade
     *            session.
     */
    public void request(Player other) {
        if (other.getTradeSession().inTradeSession()) {
            other.getMessages().sendMessage("You are already in a trade session!");
            return;
        }
        if (inTradeSession()) {
            other.getMessages().sendMessage("They are already in a trade session!");
            return;
        }
        if (player.equals(other)) {
            other.getMessages().sendMessage("You cannot initiate a trade session with yourself!");
            return;
        }
        if (player.equals(other.getTradeSession().getOther())) {
            this.other = other;
            other.getTradeSession().setOther(player);
            stage = TradeStage.OFFER;
            other.getTradeSession().setStage(TradeStage.OFFER);
            execute(TradeStage.OFFER);
            other.getTradeSession().execute(TradeStage.OFFER);
            player.facePosition(other.getPosition());
            other.facePosition(player.getPosition());
            return;
        }
        this.other = other;
        player.getMessages().sendMessage("Sending trade request...");
        other.getMessages().sendMessage(player.getFormatUsername() + ":tradereq:");
        player.facePosition(other.getPosition());
    }

    /**
     * Executes the action that corresponds with {@code stage}.
     *
     * @param stage
     *            the stage to execute the action for.
     * @throws IllegalArgumentException
     *             if the trade stage is invalid.
     */
    public void execute(TradeStage stage) {
        if (!inTradeSession())
            return;
        switch (stage) {
        case OFFER:
            String trade = name(player);
            int remaining = player.getInventory().remaining();
            player.getMessages().sendItemsOnInterface(3322, player.getInventory().container());
            player.getMessages().sendString("Trading with: " + trade + " " + "who has @gre@" + remaining + " free slots", 3417);
            player.getMessages().sendString("", 3431);
            player.getMessages().sendString("Are you sure you want to make" + " this trade?", 3535);
            player.getMessages().sendInventoryInterface(3323, 3321);
            player.getMessages().sendItemsOnInterface(3415, null, 0);
            other.getMessages().sendItemsOnInterface(3416, null, 0);
            break;
        case FIRST_ACCEPT:
            player.getMessages().sendItemsOnInterface(3214, player.getInventory().container());
            player.getMessages().sendString(getItemNames(container.container()), 3557);
            player.getMessages().sendString(getItemNames(other.getTradeSession().getContainer().container()), 3558);
            player.getMessages().sendInventoryInterface(3443, 3213);
            break;
        case FINAL_ACCEPT:
            if (other.getSession().getState() == IOState.LOGGED_IN && player.getSession().getState() == IOState.LOGGED_IN) {
                other.getInventory().addAll(container);
                reset();
            } else {
                reset(false);
            }
            break;
        default:
            throw new IllegalArgumentException("Invalid trade stage!");
        }
    }

    /**
     * Resets this trade session for both the controller and their partner.
     *
     * @param declined
     *            determines if the trade is being reset because they declined.
     */
    public void reset(boolean declined) {
        if (!inTradeSession())
            return;
        player.getInventory().addAll(container);
        other.getInventory().addAll(other.getTradeSession().getContainer());
        if (declined) {
            other.getMessages().sendMessage("The other player has declined the trade!");
            player.getMessages().sendMessage("You have declined the trade.");
        }
        other.getTradeSession().reset();
        reset();
    }

    /**
     * Determines if this controller is in an active trade session.
     *
     * @return {@code true} if the controller is in a trade, {@code false}
     *         otherwise.
     */
    public boolean inTradeSession() {
        return stage == TradeStage.OFFER || stage == TradeStage.FIRST_ACCEPT || stage == TradeStage.FINAL_ACCEPT;
    }

    /**
     * Resets the trade session for the controller.
     */
    private void reset() {
        if (!inTradeSession())
            return;
        player.getMessages().sendCloseWindows();
        container.clear();
        stage = null;
        other = null;
    }

    /**
     * Determines and returns the trade display name for {@code player}.
     *
     * @param player
     *            the player to determine this display name for.
     * @return the trade display name.
     */
    private String name(Player player) {
        return player.getFormatUsername().concat(
            player.getRights().equal(Rights.MODERATOR) ? "@cr1@" : player.getRights().greater(Rights.MODERATOR) ? "@cr2@" : "");
    }

    /**
     * Determines and returns the text for {@code items} that will be displayed
     * on the confirm trade screen.
     *
     * @param items
     *            the array of items to display.
     * @return the confirm text for the array of items.
     */
    private String getItemNames(Item[] items) {
        String tradeItems = "Absolutely nothing!";
        String tradeAmount = "";
        int count = 0;
        for (Item item : items) {
            if (item == null) {
                continue;
            }
            tradeAmount = item.getAmount() >= 1000 && item.getAmount() < 1000000
                ? "@cya@" + (item.getAmount() / 1000) + "K @whi@" + "(" + item.getAmount() + ")" : item.getAmount() >= 1000000
                    ? "@gre@" + (item.getAmount() / 1000000) + " " + "million @whi@(" + item.getAmount() + ")" : "" + item.getAmount();
            tradeItems = count == 0 ? item.getDefinition().getName() : tradeItems + "\\n" + item.getDefinition().getName();
            if (item.getDefinition().isStackable())
                tradeItems = tradeItems + " x " + tradeAmount;
            count++;
        }
        return tradeItems;
    }

    /**
     * Gets the player that controls this trade session.
     *
     * @return the player in control.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the container of items in this trade session.
     *
     * @return the container of items.
     */
    public ItemContainer getContainer() {
        return container;
    }

    /**
     * Gets the other player in this trade session.
     *
     * @return the other player.
     */
    public Player getOther() {
        return other;
    }

    /**
     * Sets the value for {@link TradeSession#other}.
     *
     * @param other
     *            the new value to set.
     */
    public void setOther(Player other) {
        this.other = other;
    }

    /**
     * Gets the stage that this trade session is in
     *
     * @return the stage of the trade session.
     */
    public TradeStage getStage() {
        return stage;
    }

    /**
     * Sets the value for {@link TradeSession#stage}.
     *
     * @param stage
     *            the new value to set.
     */
    public void setStage(TradeStage stage) {
        this.stage = stage;
    }
}
