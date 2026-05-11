import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

/**
 * MarketGUI.java  —  Main entry point for the Stock Market Simulator.
 *
 * Design: Material You 3 shapes + Anthropic/Claude minimalism.
 * Trade and Portfolio open as floating popovers from header buttons.
 * All game logic lives untouched in the existing classes.
 * MarketRunner is used as a static data store only; its main() is NOT called.
 */
public class MarketGUI extends JFrame {

    // ─── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(0x0D0D10);
    private static final Color SURFACE     = new Color(0x18181C);
    private static final Color SURFACE_VAR = new Color(0x222228);
    private static final Color SURFACE_HVR = new Color(0x2A2A32);
    private static final Color ACCENT      = new Color(0xD4714A);
    private static final Color ON_SURFACE  = new Color(0xE8E8EE);
    private static final Color MUTED       = new Color(0x76768A);
    private static final Color GAIN        = new Color(0x3DB87A);
    private static final Color GAIN_DIM    = new Color(0x112B1E);
    private static final Color LOSS        = new Color(0xE05252);
    private static final Color LOSS_DIM    = new Color(0x2B1111);
    private static final Color BORDER      = new Color(0x28282F);

    // ─── Game state ───────────────────────────────────────────────────────────
    private final Player     player;
    private final MarketGrid gameGrid;

    // ─── Grid UI refs ─────────────────────────────────────────────────────────
    private final JLabel[]     priceLbls  = new JLabel[100];
    private final JLabel[]     deltaLbls  = new JLabel[100];
    private final RoundPanel[] cellPanels = new RoundPanel[100];
    private final javax.swing.Timer[] clearTimers = new javax.swing.Timer[100];

    // ─── Header widgets ───────────────────────────────────────────────────────
    private JLabel  balanceLbl;
    private JLabel  worthLbl;
    private JLabel  timerLbl;
    private JButton tradeBtn;
    private JButton portBtn;

    // ─── Popovers ─────────────────────────────────────────────────────────────
    private JWindow tradePopover;
    private JWindow portPopover;

    // ─── Popover-internal widgets ─────────────────────────────────────────────
    private JTextField symbolField;
    private JTextField sharesField;
    private JPanel     portfolioList;
    private JTextArea  eventLog;

    // ─── Timers ───────────────────────────────────────────────────────────────
    private javax.swing.Timer marketTimer;
    private javax.swing.Timer countdownTimer;
    private int secondsLeft = 60;

    // ─── Fonts ────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = resolveFont("SansSerif", Font.BOLD,  17);
    private static final Font FONT_BODY   = resolveFont("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_SMALL  = resolveFont("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_BOLD   = resolveFont("SansSerif", Font.BOLD,  13);
    private static final Font FONT_MONO   = resolveMono(11);
    private static final Font FONT_MONO_S = resolveMono(10);

    // ─────────────────────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        MarketRunner.loadEventsFromCSV("EventData.csv");
        MarketRunner.loadStocksFromCSV("StockData.csv");
        MarketRunner.simulationGrid = new Stock[10][10];

        SwingUtilities.invokeLater(() -> {
            applyBaseUI();
            new MarketGUI().setVisible(true);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public MarketGUI() {
        super("Wallet Simulator");

        player   = new Player();
        gameGrid = new MarketGrid(MarketRunner.simulationGrid);
        gameGrid.loadGrid(MarketRunner.Stocks);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setPreferredSize(new Dimension(1200, 860));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        // Build popovers after pack() so screen coordinates are valid
        tradePopover = buildTradePopover();
        portPopover  = buildPortfolioPopover();

        setupGlobalDismiss();
        startTimers();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Header bar
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SURFACE);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        // Left: brand  |  [Trade ▾]  [Portfolio ▾]  |  Cash  |  Net Worth
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JLabel brand = lbl("Wallet Simulator", FONT_TITLE, ACCENT);

        tradeBtn = popoverToggleBtn("Trade  ▾");
        portBtn  = popoverToggleBtn("Portfolio  ▾");

        tradeBtn.addActionListener(e -> togglePopover(tradePopover, tradeBtn, portPopover));
        portBtn .addActionListener(e -> togglePopover(portPopover,  portBtn,  tradePopover));

        balanceLbl = lbl("Cash  $10,000.00",      FONT_BODY, ON_SURFACE);
        worthLbl   = lbl("Net Worth  $10,000.00", FONT_BODY, MUTED);

        left.add(brand);
        left.add(vSep());
        left.add(tradeBtn);
        left.add(portBtn);
        left.add(vSep());
        left.add(balanceLbl);
        left.add(vSep());
        left.add(worthLbl);

        // Right: countdown + quit
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        timerLbl = lbl("⏱  1:00", FONT_BOLD, MUTED);

        JButton quit = pillButton("✕  Quit", new Color(0x5C1C1C), new Color(0xFF9999));
        quit.addActionListener(e -> shutdown());

        right.add(timerLbl);
        right.add(quit);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Center — grid fills the entire area
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.add(buildGridArea(), BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  10 × 10 Stock Grid
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildGridArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JLabel title = lbl("Live Market", FONT_BOLD, ON_SURFACE);
        JLabel hint  = lbl("Click a cell to fill the Trade panel", FONT_SMALL, MUTED);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(title, BorderLayout.WEST);
        topRow.add(hint,  BorderLayout.EAST);
        wrapper.add(topRow, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(10, 10, 6, 6));
        grid.setBackground(BG);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int   idx = r * 10 + c;
                Stock s   = MarketRunner.simulationGrid[r][c];

                RoundPanel cell = new RoundPanel(SURFACE, 10);
                cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
                cell.setBorder(new EmptyBorder(8, 10, 8, 10));

                JLabel symLbl   = lbl(trunc(s.getAbbrevName(), 9), FONT_MONO_S.deriveFont(Font.BOLD), ON_SURFACE);
                JLabel priceLbl = lbl("$" + f2(s.getStockValue()),  FONT_MONO_S,                      MUTED);
                JLabel deltaLbl = lbl("",                            FONT_MONO_S.deriveFont(Font.BOLD), GAIN);

                symLbl  .setAlignmentX(Component.CENTER_ALIGNMENT);
                priceLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                deltaLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                cell.add(symLbl);
                cell.add(priceLbl);
                cell.add(deltaLbl);

                cellPanels[idx] = cell;
                priceLbls [idx] = priceLbl;
                deltaLbls [idx] = deltaLbl;

                final String abbrev = s.getAbbrevName();
                cell.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        // Fill symbol and open Trade popover if not already open
                        symbolField.setText(abbrev);
                        if (!tradePopover.isVisible()) {
                            togglePopover(tradePopover, tradeBtn, portPopover);
                        }
                        sharesField.requestFocus();
                    }
                    public void mouseEntered(MouseEvent e) {
                        cell.setBackground(SURFACE_HVR);
                        cell.repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        cell.setBackground(SURFACE);
                        cell.repaint();
                    }
                });
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                grid.add(cell);
            }
        }

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Trade popover window
    // ─────────────────────────────────────────────────────────────────────────
    private JWindow buildTradePopover() {
        JWindow w = new JWindow(this);
        w.setBackground(new Color(0, 0, 0, 0));

        PopoverPanel content = new PopoverPanel(SURFACE, 14);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(18, 18, 26, 26)); // extra bottom/right for shadow

        content.add(sectionLabel("Stock Symbol"));
        content.add(Box.createVerticalStrut(6));
        symbolField = styledField();
        content.add(symbolField);
        content.add(Box.createVerticalStrut(14));

        content.add(sectionLabel("Number of Shares"));
        content.add(Box.createVerticalStrut(6));
        sharesField = styledField();
        content.add(sharesField);
        content.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton buyBtn  = pillButton("↑  Buy",  GAIN_DIM, GAIN);
        JButton sellBtn = pillButton("↓  Sell", LOSS_DIM, LOSS);
        buyBtn .addActionListener(e -> handleBuy());
        sellBtn.addActionListener(e -> handleSell());
        btnRow.add(buyBtn);
        btnRow.add(sellBtn);
        content.add(btnRow);
        content.add(Box.createVerticalStrut(22));

        content.add(sectionLabel("Event Log"));
        content.add(Box.createVerticalStrut(6));

        eventLog = new JTextArea();
        eventLog.setEditable(false);
        eventLog.setBackground(SURFACE_VAR);
        eventLog.setForeground(MUTED);
        eventLog.setFont(FONT_MONO);
        eventLog.setLineWrap(true);
        eventLog.setWrapStyleWord(true);
        eventLog.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane logPane = new JScrollPane(eventLog);
        logPane.setBorder(new LineBorder(BORDER, 1));
        logPane.setBackground(SURFACE_VAR);
        logPane.getViewport().setBackground(SURFACE_VAR);
        logPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        logPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        logPane.setPreferredSize(new Dimension(0, 180));
        content.add(logPane);

        w.add(content);
        w.setSize(310, 500);
        return w;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Portfolio popover window
    // ─────────────────────────────────────────────────────────────────────────
    private JWindow buildPortfolioPopover() {
        JWindow w = new JWindow(this);
        w.setBackground(new Color(0, 0, 0, 0));

        PopoverPanel content = new PopoverPanel(SURFACE, 14);
        content.setLayout(new BorderLayout(0, 12));
        content.setBorder(new EmptyBorder(18, 18, 26, 26));

        content.add(lbl("Your Holdings", FONT_BOLD, ON_SURFACE), BorderLayout.NORTH);

        portfolioList = new JPanel();
        portfolioList.setBackground(SURFACE);
        portfolioList.setLayout(new BoxLayout(portfolioList, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(portfolioList);
        scroll.setBorder(new LineBorder(BORDER, 1));
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(SURFACE);
        content.add(scroll, BorderLayout.CENTER);

        w.add(content);
        w.setSize(330, 420);
        return w;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Popover toggle — positions window below anchor button, closes sibling
    // ─────────────────────────────────────────────────────────────────────────
    private void togglePopover(JWindow target, JButton anchor, JWindow other) {
        if (other.isVisible()) other.setVisible(false);

        if (target.isVisible()) {
            target.setVisible(false);
        } else {
            Point loc = anchor.getLocationOnScreen();
            int   x   = loc.x;
            int   y   = loc.y + anchor.getHeight() + 6;

            // Keep popover from going off-screen to the right
            int screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
            if (x + target.getWidth() > screenW - 10) {
                x = screenW - target.getWidth() - 10;
            }

            target.setLocation(x, y);
            target.setVisible(true);
            target.toFront();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Global dismiss — click anywhere outside a popover closes it
    // ─────────────────────────────────────────────────────────────────────────
    private void setupGlobalDismiss() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event.getID() != MouseEvent.MOUSE_PRESSED) return;
            MouseEvent me = (MouseEvent) event;
            Point p = me.getLocationOnScreen();
            dismissIfOutside(tradePopover, tradeBtn, p);
            dismissIfOutside(portPopover,  portBtn,  p);
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    private void dismissIfOutside(JWindow pop, JButton toggleBtn, Point click) {
        if (!pop.isVisible()) return;
        Rectangle popBounds = pop.getBounds();
        Rectangle btnBounds = new Rectangle(
            toggleBtn.getLocationOnScreen(), toggleBtn.getSize()
        );
        // Don't close if clicking inside the popover or on its own toggle button
        if (!popBounds.contains(click) && !btnBounds.contains(click)) {
            pop.setVisible(false);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Trade handlers
    // ─────────────────────────────────────────────────────────────────────────
    private void handleBuy() {
        String sym    = symbolField.getText().trim().toUpperCase();
        int    shares = parseShares();
        if (sym.isEmpty() || shares <= 0) { flashError(sharesField); return; }

        double[] before = snapshot();
        player.buyStock(sym, shares, gameGrid);
        afterTrade(before);
    }

    private void handleSell() {
        String sym    = symbolField.getText().trim().toUpperCase();
        int    shares = parseShares();
        if (sym.isEmpty() || shares <= 0) { flashError(sharesField); return; }

        double[] before = snapshot();
        player.sellStock(sym, shares, gameGrid);
        afterTrade(before);
    }

    private void afterTrade(double[] before) {
        flashDeltas(before);
        updateHeader();
        refreshPortfolio();
        symbolField.setText("");
        sharesField.setText("");
    }

    private int parseShares() {
        try   { return Integer.parseInt(sharesField.getText().trim()); }
        catch (NumberFormatException ex) { return 0; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Timers  (both fire on EDT via javax.swing.Timer)
    // ─────────────────────────────────────────────────────────────────────────
    private void startTimers() {
        countdownTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft = Math.max(0, secondsLeft - 1);
            timerLbl.setText("⏱  " + secondsLeft / 60 + ":" + String.format("%02d", secondsLeft % 60));
        });
        countdownTimer.start();

        marketTimer = new javax.swing.Timer(60_000, e -> {
            secondsLeft = 60;
            double[] before = snapshot();

            MarketLogic.updateFullMarket(MarketRunner.simulationGrid);

            Event triggered = Randomizer.chooseEvent(MarketRunner.Events);
            if (triggered != null) {
                EventManager.applyEvent(triggered, MarketRunner.simulationGrid);
                appendLog("[EVENT] " + triggered.getEventName()
                    + "  (" + triggered.getTargetType()
                    + "  price " + triggered.getPriceEffect()
                    + "%  shares " + triggered.getStockEffect() + ")");
            } else {
                appendLog("[MARKET] Prices updated.");
            }

            player.updateNetWorth();
            flashDeltas(before);
            updateHeader();
            refreshPortfolio();
        });
        marketTimer.start();
    }

    private void appendLog(String msg) {
        eventLog.append(msg + "\n");
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Price flash — green/red delta on every changed cell for 2.5 s
    // ─────────────────────────────────────────────────────────────────────────
    private double[] snapshot() {
        double[] snap = new double[100];
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 10; c++)
                snap[r * 10 + c] = MarketRunner.simulationGrid[r][c].getStockValue();
        return snap;
    }

    private void flashDeltas(double[] before) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int    idx  = r * 10 + c;
                Stock  s    = MarketRunner.simulationGrid[r][c];
                double now  = s.getStockValue();
                double diff = now - before[idx];

                priceLbls[idx].setText("$" + f2(now));

                if (Math.abs(diff) < 0.005) continue;

                boolean up    = diff > 0;
                Color   clr   = up ? GAIN    : LOSS;
                Color   dimBg = up ? GAIN_DIM : LOSS_DIM;
                String  sign  = up ? "+"      : "";

                deltaLbls[idx].setText(sign + f2(diff));
                deltaLbls[idx].setForeground(clr);
                cellPanels[idx].setBackground(dimBg);
                cellPanels[idx].setBorderColor(clr);
                cellPanels[idx].repaint();

                if (clearTimers[idx] != null) clearTimers[idx].stop();

                final int fi = idx;
                clearTimers[fi] = new javax.swing.Timer(5000, ev -> {
                    deltaLbls[fi].setText("");
                    cellPanels[fi].setBackground(SURFACE);
                    cellPanels[fi].setBorderColor(null);
                    cellPanels[fi].repaint();
                    clearTimers[fi] = null;
                });
                clearTimers[fi].setRepeats(false);
                clearTimers[fi].start();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Header and portfolio refresh
    // ─────────────────────────────────────────────────────────────────────────
    private void updateHeader() {
        balanceLbl.setText("Cash  $"      + f2(player.getAllowance()));
        worthLbl  .setText("Net Worth  $" + f2(player.getNetWorth()));
    }

    private void refreshPortfolio() {
        portfolioList.removeAll();
        ArrayList<PlayerStock> port = player.viewPortfolio();
        boolean anyHolding = false;
        boolean stripe     = false;

        for (int i = 0; i < port.size(); i++) {
            PlayerStock ps = port.get(i);
            if (ps.getSharesOwned() <= 0) continue;
            anyHolding = true;

            JPanel row = new JPanel(new BorderLayout(8, 2));
            row.setBackground(stripe ? SURFACE_VAR : SURFACE);
            row.setBorder(new EmptyBorder(10, 14, 10, 14));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(lbl(ps.getStock().getAbbrevName(), FONT_BOLD, ON_SURFACE));
            left.add(lbl(ps.getSharesOwned() + " shares  @  $"
                + f2(ps.getStock().getStockValue()), FONT_SMALL, MUTED));

            double totalVal = ps.getSharesOwned() * ps.getStock().getStockValue();
            JLabel valLbl   = lbl("$" + f2(totalVal), FONT_BOLD, GAIN);

            row.add(left,   BorderLayout.WEST);
            row.add(valLbl, BorderLayout.EAST);
            portfolioList.add(row);
            stripe = !stripe;
        }

        if (!anyHolding) {
            JLabel empty = lbl("No holdings yet.", FONT_SMALL.deriveFont(Font.ITALIC), MUTED);
            empty.setBorder(new EmptyBorder(12, 14, 12, 14));
            portfolioList.add(empty);
        }

        portfolioList.revalidate();
        portfolioList.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Shutdown
    // ─────────────────────────────────────────────────────────────────────────
    private void shutdown() {
        if (marketTimer    != null) marketTimer.stop();
        if (countdownTimer != null) countdownTimer.stop();
        dispose();
        System.exit(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI component helpers
    // ─────────────────────────────────────────────────────────────────────────
    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = lbl(text, FONT_SMALL, MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setBackground(SURFACE_VAR);
        f.setForeground(ON_SURFACE);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_BODY);
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(
            new RoundLineBorder(BORDER, 1, 10),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private void flashError(JTextField field) {
        field.setBorder(new CompoundBorder(
            new RoundLineBorder(LOSS, 1, 10),
            new EmptyBorder(8, 12, 8, 12)
        ));
        javax.swing.Timer t = new javax.swing.Timer(900, e ->
            field.setBorder(new CompoundBorder(
                new RoundLineBorder(BORDER, 1, 10),
                new EmptyBorder(8, 12, 8, 12)
            ))
        );
        t.setRepeats(false);
        t.start();
    }

    private JButton pillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BOLD);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton popoverToggleBtn(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()
                    ? SURFACE_HVR
                    : getModel().isRollover() ? SURFACE_VAR : new Color(0, 0, 0, 0);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(ON_SURFACE);
        btn.setFont(FONT_BOLD);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator vSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 16));
        s.setForeground(BORDER);
        return s;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utility
    // ─────────────────────────────────────────────────────────────────────────
    private static String f2(double v) { return String.format("%.2f", v); }

    private static String trunc(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static Font resolveFont(String fallback, int style, int size) {
        String[] candidates = { "SF Pro Display", "Segoe UI", "Helvetica Neue", "Ubuntu", "Cantarell" };
        Set<String> avail = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        ));
        for (String c : candidates) {
            if (avail.contains(c)) return new Font(c, style, size);
        }
        return new Font(fallback, style, size);
    }

    private static Font resolveMono(int size) {
        String[] candidates = { "JetBrains Mono", "Cascadia Code", "Fira Code", "Consolas", "Menlo", "Courier New" };
        Set<String> avail = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        ));
        for (String c : candidates) {
            if (avail.contains(c)) return new Font(c, Font.PLAIN, size);
        }
        return new Font("Monospaced", Font.PLAIN, size);
    }

    private static void applyBaseUI() {
        UIManager.put("ScrollBar.thumb", new Color(0x3A3A45));
        UIManager.put("ScrollBar.track", SURFACE_VAR);
        UIManager.put("ScrollBar.width", 6);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RoundPanel — grid cell with rounded rect + optional colored border
    // ─────────────────────────────────────────────────────────────────────────
    static class RoundPanel extends JPanel {
        private final int radius;
        private Color borderColor;

        RoundPanel(Color bg, int radius) {
            this.radius = radius;
            setBackground(bg);
            setOpaque(false);
        }

        void setBorderColor(Color c) { borderColor = c; }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PopoverPanel — popover container with drop shadow
    // ─────────────────────────────────────────────────────────────────────────
    static class PopoverPanel extends JPanel {
        private final int radius;
        private static final int SHADOW = 8;

        PopoverPanel(Color bg, int radius) {
            this.radius = radius;
            setBackground(bg);
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Layered translucent shadow
            for (int i = SHADOW; i > 0; i--) {
                int alpha = (int) (55.0 * (SHADOW - i + 1) / SHADOW);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius + i, radius + i);
            }

            // Surface fill
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - SHADOW, getHeight() - SHADOW, radius, radius);

            // Thin border
            g2.setColor(new Color(0x3A3A48));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - SHADOW - 1, getHeight() - SHADOW - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }

        public Insets getInsets() {
            Insets i = super.getInsets();
            return new Insets(i.top, i.left, i.bottom + SHADOW, i.right + SHADOW);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RoundLineBorder — rounded-corner border for text fields
    // ─────────────────────────────────────────────────────────────────────────
    static class RoundLineBorder extends AbstractBorder {
        private final Color color;
        private final int   thickness;
        private final int   radius;

        RoundLineBorder(Color color, int thickness, int radius) {
            this.color     = color;
            this.thickness = thickness;
            this.radius    = radius;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
    }
}
