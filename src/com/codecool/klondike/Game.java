package com.codecool.klondike;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;

import java.util.*;


public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

	private Boolean variant=false;
	
    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

	private static double DISCARD_GAP = 1;
    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;
    private static double STOCK_X_POS = 95;
    private static double STOCK_DISC_Y_POS = 20;
    private static double DISCARD_X_POS = 285;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
		int numerOfCards;
        Card card = (Card) e.getSource();
        System.out.println(e.getClickCount());
		if (this.variant==true){
			numerOfCards=2;
			System.out.println("Jestem w ifie");
			//discardPile.getTopCard().setLayoutX(getLayoutX()+170);
		}
		else{
			numerOfCards=0;
			System.out.println("Jestem w elsie");
		}
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
			card=card.getContainingPile().getCardByIndex(0);
			for(int i=0; i<=numerOfCards; i++){
				if(!stockPile.isEmpty()){
					card.getContainingPile().getTopCard().flip();
					card.getContainingPile().getTopCard().moveToPile(discardPile);
					card.getContainingPile().getTopCard().setMouseTransparent(false);
					System.out.println("Placed " + card + " to the waste. Totu, tak");
				}
			}
        }
        if(e.getClickCount() == 2){
            for (Pile destPile : foundationPiles){
                if(!destPile.isEmpty()){

                    Card card2 = destPile.getTopCard();

                    if(isMoveValid(card, destPile)){
                        if(Card.isSameSuit(card, card2) && card2.getRank() == card.getRank()-1){
                            card.moveToPile(destPile);
                        }
                    }
                }
            }
        }
		flipTops();
		
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
        flipTops();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (!isDragable(card)){
            return;
        }
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;
		
		int size=activePile.numOfCards();
		//while(card.toString.equals(activePile.get(size).toString))
		//{}
		int index=activePile.getCardIndex(card);	
        draggedCards.clear();
		if(!activePile.getPileType().equals(Pile.PileType.DISCARD))
		{
		for(int i=index; i<activePile.numOfCards();i++)
        	draggedCards.add(activePile.getCardByIndex(i));
		}//do{
		else
			draggedCards.add(activePile.getTopCard());
			
		//}
		for(Card x : draggedCards){
			x.getDropShadow().setRadius(20);
			x.getDropShadow().setOffsetX(10);
			x.getDropShadow().setOffsetY(10);

			x.toFront();
			x.setTranslateX(offsetX);
			x.setTranslateY(offsetY);
        }
        flipTops();
    };
        private boolean isDragable(Card card){
            Pile activePile = card.getContainingPile();
            if(activePile.getPileType() == Pile.PileType.STOCK){
                return false;
            }
            if(activePile.getPileType() == Pile.PileType.DISCARD && !card.equals(card.getContainingPile().getTopCard())){
                return false;
            }
            if(activePile.getPileType() == Pile.PileType.FOUNDATION && !card.equals(card.getContainingPile().getTopCard())){
                return false;
            }
            if(card.isFaceDown()){
                return false;
            }
            return true;
        }

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        Pile pileFoun = getValidIntersectingPile(card, foundationPiles);

        // TODO
        if (pile != null) {
            if (isMoveValid(card, pile))
                handleValidMove(card, pile);
        } else if (pileFoun != null) {
            if (isMoveValid(card, pileFoun))
                handleValidMove(card, pileFoun);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
        flipTops();
        // if(isGameWon){
        //     youWon();
        // }
    };

    public boolean isGameWon() {
        int cardSum = 0;
        for(Pile pile : foundationPiles) {
            cardSum += pile.numOfCards();
        }
        if(cardSum == 52){
            return true;
        }
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        initButtons();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        
        ObservableList<Card> pileCards = discardPile.getCards();
		ObservableList<Card> pileCards2 = stockPile.getCards();
            
           // Collections.reverse(pileCards);
            for(int i=pileCards.size()-1; i>=0; i--){
                if (!pileCards.get(i).isFaceDown()){
                    pileCards.get(i).flip();
                }
               // stockPile.addCard(thrownCard);
				//pileCards.getTopCard().moveToPile(stockPile);
				pileCards.get(i).moveToPile(stockPile);
            }
    
            // stockPile.setLayoutX(STOCK_X_POS);
            // stockPile.setLayoutY(STOCK_DISC_Y_POS);
		
            // discardPile.setLayoutX(DISCARD_X_POS);
            // discardPile.setLayoutY(STOCK_DISC_Y_POS);

        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.isEmpty() && destPile.getPileType().equals(Pile.PileType.TABLEAU))
            if (card.getRank() == 13) {
                System.out.println("Ten stos był pusty, za prawdę.");
                return true;
            }

        if (!destPile.isEmpty() && destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (Card.isOppositeColor(card, destPile.getTopCard())
                    && destPile.getTopCard().getRank() == card.getRank() + 1)
                return true;
        }

        if (destPile.isEmpty() && destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            if (card.getRank() == 1) {
                System.out.println("Aye, aye, oh, shake your foundations.");
                return true;
            }
        }
        if (!destPile.isEmpty() && destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            Card card1 = card;
            Card card2 = destPile.getTopCard();
            if (Card.isSameSuit(card1, card2) && card2.getRank() == card1.getRank() - 1) {
                return true;
            }
        }

        return false; // ZMIENIĆ NA FALSE!!!
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) && isOverPile(card, pile) && isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        for(Card cardDrag : draggedCards){
            card.getContainingPile().getCards().remove(cardDrag);
        }
        draggedCards.clear();
		
    }
	
	private void initButtons(){
	Button undo = new Button("Undo");
	undo.setLayoutX(0);
	undo.setLayoutY(0);
	getChildren().add(undo);
		
	Button restart = new Button("Restart");
	restart.setLayoutX(0);
	restart.setLayoutY(27);
	getChildren().add(restart);
		
	Button changeVariant = new Button("Change Variant");
	changeVariant.setLayoutX(0);
	changeVariant.setLayoutY(54);
	getChildren().add(changeVariant);
		
		
		
	changeVariant.setOnAction( event ->{
		if (this.variant==false){
			this.variant=true;
		System.out.println("Wariant to false");

		}
		else{
			this.variant=false;
		System.out.println("Wariant to true");
		}
		System.out.println("Czy aby napewno Cie klikłem?");
		
		
	});
	
	
	undo.setOnAction( event ->{
	});
	
	restart.setOnAction(event->{
		
		
			
		for(Pile pile : foundationPiles){
			System.out.println(pile.numOfCards());
			if(!pile.isEmpty()){
				for(int i=pile.numOfCards(); i>0;i--){
					pile.getCardByIndex(i-1).flip(); 
					pile.getCardByIndex(i-1).moveToPile(stockPile);
				
				}
			}
			
		}
		for(Pile pile : tableauPiles){
			System.out.println(pile.numOfCards());
			if(!pile.isEmpty()){
				for(int i=pile.numOfCards(); i>0;i--){
                    if(!pile.getCardByIndex(i-1).isFaceDown())
                        pile.getCardByIndex(i-1).flip(); 
                    
					pile.getCardByIndex(i-1).moveToPile(stockPile);
				
				}
			}
			
		}
		for(int i=discardPile.numOfCards();i>0;i--){
			discardPile.getCardByIndex(i-1).flip();
			discardPile.getCardByIndex(i-1).moveToPile(stockPile);
			
		}
		Collections.shuffle(stockPile.getCards());
		for(int i=stockPile.numOfCards();i>0;i--)
			stockPile.getCardByIndex(i-1).moveToPile(discardPile);
		for(int i=discardPile.numOfCards();i>0;i--)
			discardPile.getCardByIndex(i-1).moveToPile(stockPile);
           
         for(int i = 0; i < 7; i++){
             for(int j = 0; j < i+1; j++){
                 Card theCard = stockPile.getCards().remove(0);
                 tableauPiles.get(6-j).addCard(theCard);
             }
         }
         flipTops();
		
	});
		
	
	}

    private void initPiles() {
        // Button undo = new Button("Undo");
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(STOCK_X_POS);
        stockPile.setLayoutY(STOCK_DISC_Y_POS);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);
        // getChildren().add(undo);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", DISCARD_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(DISCARD_X_POS);
        discardPile.setLayoutY(STOCK_DISC_Y_POS);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        // TODO
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
        
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < i+1; j++){
                Card theCard = stockPile.getCards().remove(0);
                tableauPiles.get(6-j).addCard(theCard);
            }
        }
        flipTops();       

    }
    private void flipTops(){
        for(int i = 0; i < 7; i++){
            Card topCard = tableauPiles.get(i).getTopCard();
            if(topCard != null && topCard.isFaceDown()){
                topCard.flip();
            }
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
