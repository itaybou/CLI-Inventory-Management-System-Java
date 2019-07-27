package logic;

import logic.models.DefectiveProduct;
import logic.models.StockProducts;
import logic.models.StoreBranch;
import logic.datatypes.Date;
import presentation.Prompter;
import presentation.printers.BranchesPrinter;
import presistence.Repository;
import presistence.dao.BranchesDAO;
import presistence.dao.DefectiveDAO;
import presistence.dao.StockDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to communicate between End-user and the Database
     * and to perform all logical program actions.
     */
    public class Interactor {

        private static Interactor instance = null;

        private static Map<Modules, Processor> processors;

        private static boolean shutdown; //Determines whether the programs needs to shutdown.
        private static StoreBranch activeStoreBranch = null; //Current connected store branch
        private static Date currentDate;

        private static Repository repo; //A repository instance used to communicate with database
        private static Prompter prompter; //A CLI prompter instance used to present and retrieve date to and from end-user

        //Constructor
        private Interactor(Prompter prompt)
        {
            repo = new Repository();
            prompter = prompt;
            shutdown = false;
            processors = new HashMap<>() {{
                    put(Modules.PRODUCTS, new ProductsProcessor(prompter, repo));
                    put(Modules.DISCOUNTS, new DiscountProcessor(prompter, repo));
                    put(Modules.CATEGORIES, new CategoryProcessor(prompter, repo));
                    put(Modules.STOCK, new StockProcessor(prompter, repo));
                    put(Modules.REPORTS, new ReportsProcessor(prompter, repo));
                    put(Modules.DEFECTS, new DefectsProcessor(prompter, repo));
            }};
            repo.connect();
        }

        public static Interactor getInstance()
        {
            if (instance == null)
                instance = new Interactor(new Prompter());
            return instance;
        }

    /**
     * Main processor loop to run until end-user chose to exist and shutdown flag is changed.
     */
    public void start()
    {
        currentDate = Date.getCurrentDate();
        ((ReportsProcessor)processors.get(Modules.REPORTS)).setCurrentDate(currentDate);
        ((DefectsProcessor)processors.get(Modules.DEFECTS)).setCurrentDate(currentDate);
        while(!shutdown) {
            while (!logToBranch()) //While no user is connected prompt user to login
                prompter.printMessage(true, "Illegal store branch ID.");
            if(shutdown)
                break;
            ((ReportsProcessor)processors.get(Modules.REPORTS)).setActiveBranch(activeStoreBranch);
            ((StockProcessor)processors.get(Modules.STOCK)).setActiveBranch(activeStoreBranch);
            ((DefectsProcessor)processors.get(Modules.DEFECTS)).setActiveBranch(activeStoreBranch);
            Map.Entry<List<StockProducts>, List<StockProducts>> expired = ((StockDAO)repo.getDAO(Modules.STOCK)).findExpired(activeStoreBranch);
            List<StockProducts> critical_amount = ((StockDAO)repo.getDAO(Modules.STOCK)).findCriticalAmount(activeStoreBranch);
            if(expired.getKey().size() != 0 || critical_amount.size() != 0)
                prompter.promptNotifications(expired.getKey(), critical_amount, currentDate);
            moveExpiredToDefects(expired);
            prompter.printMessage(false, "Welcome to Store Branch "+activeStoreBranch.getName()+" No."+activeStoreBranch.getBranchID());
            while (processMainMenu()) {
                currentDate = Date.getCurrentDate();//As long as the user is connected prompt the main menu options.
                ((ReportsProcessor)processors.get(Modules.REPORTS)).setCurrentDate(currentDate);
                ((DefectsProcessor)processors.get(Modules.DEFECTS)).setCurrentDate(currentDate);
            }
        }
        //After user chose to exit, close all object instances.
        repo.closeConnection();
        prompter.closePrompter();
    }

    private void moveExpiredToDefects(Map.Entry<List<StockProducts>, List<StockProducts>> expired) {
        for(StockProducts ex : expired.getKey()) {
            ((StockDAO)repo.getDAO(Modules.STOCK)).delete(ex);
            ((DefectiveDAO)repo.getDAO(Modules.DEFECTS)).insert(new DefectiveProduct(ex.getBarcode(), ex.getExpiration_date(), ex.getLocationID(),
                                                                    ex.getQuantity(), "Expiration Date", currentDate));
        }

        for(StockProducts ex : expired.getValue()) {
            ((StockDAO)repo.getDAO(Modules.STOCK)).delete(ex);
            ((DefectiveDAO)repo.getDAO(Modules.DEFECTS)).insert(new DefectiveProduct(ex.getBarcode(), ex.getExpiration_date(), ex.getLocationID(),
                    ex.getQuantity(), "Expiration Date", currentDate));
        }
    }

    public boolean logToBranch()
    {
        try {
            while (activeStoreBranch == null) {
                switch (prompter.getPrinter(Modules.BRANCHES).printMenu()) {
                    case 1:
                        prompter.printMessage(false, null);
                        if((activeStoreBranch = ((BranchesDAO)repo.getDAO(Modules.BRANCHES)).findByKey(
                                new StoreBranch(((BranchesPrinter)prompter.getPrinter(Modules.BRANCHES)).chooseBranch(false)))) == null)
                            throw new IllegalArgumentException();
                        break;
                    case 2:
                        processOpenNewBranch();
                        break;
                    case 3:
                        processCloseBranch();
                        break;
                    case 4:
                        processViewChain();
                        break;
                    case 5:
                        prompter.printMessage(false, "Goodbye, hope you enjoyed our system.");
                        shutdown = true;
                        return true;

                }
            }
        } catch (Exception e) {
            prompter.printMessage(true, "Illegal store branch ID.");
            return false;
        }
        return true;
    }

    private void processOpenNewBranch() throws IOException {
        prompter.printMessage(false, null);
        String name = ((BranchesPrinter)prompter.getPrinter(Modules.BRANCHES)).promptNewBranchName();
        if(name.equals("Q") || name.equals("q")) {
            prompter.printMessage(false, null);
            return;
        }
        else if(name.length() < 3){
            prompter.printMessage(true, "Branch name must be at least 3 letters.");
            return;
        }
        while(((BranchesDAO)repo.getDAO(Modules.BRANCHES)).findByName(new StoreBranch(name)) != null)
        {
            prompter.printMessage(true, "Store branch name already exists in system!\nPlease choose another name.");
            name = ((BranchesPrinter)prompter.getPrinter(Modules.BRANCHES)).promptNewBranchName();
            if(name.equals("Q") || name.equals("q")) {
                prompter.printMessage(false, null);
                return;
            }
            else if(name.length() < 3){
                prompter.printMessage(true, "Branch name must be at least 3 letters.");
                return;
            }
        }
        ((BranchesDAO)repo.getDAO(Modules.BRANCHES)).insert(new StoreBranch(name));
        int branchID = ((BranchesDAO)repo.getDAO(Modules.BRANCHES)).findByName(new StoreBranch(name)).getBranchID();
        prompter.printMessage(false, "Congratulations! A new store branch named:\n"
                                                +"'" + name + "' has been opened for business.\n" +
                                                "New branch ID is: "+branchID);
    }

    private void processCloseBranch() throws IOException {
        prompter.printMessage(null, "BEWARE, DELETING A STORE BRANCH WILL DELETE ENTIRE BRANCH STOCK DATA");
        int branchID = ((BranchesPrinter)prompter.getPrinter(Modules.BRANCHES)).chooseBranch(true);
        if (((BranchesDAO)repo.getDAO(Modules.BRANCHES)).findByKey(new StoreBranch(branchID)) != null) {
            Boolean delete = prompter.deletePrompt();
            while (delete == null) {
                prompter.printMessage(null, "Illegal Choice, please choose Y\\N\n" +
                                                        "BEWARE, DELETING A STORE BRANCH WILL DELETE ENTIRE BRANCH STOCK DATA");
                delete = prompter.deletePrompt();
            }
            if(delete) {
                ((BranchesDAO) repo.getDAO(Modules.BRANCHES)).delete(new StoreBranch(branchID));
                prompter.printMessage(false, "Branch ID "+branchID+" deleted successfully!");
            }
        } else prompter.printMessage(true, "Branch ID entered does not exist.");
    }

    private void processViewChain() {
        ((BranchesPrinter)prompter.getPrinter(Modules.BRANCHES)).printChainBranches(((BranchesDAO)repo.getDAO(Modules.BRANCHES)).findAll());
        prompter.printMessage(false, null);
    }

    private boolean processMainMenu()
    {
        switch(prompter.mainMenu(activeStoreBranch))
        {
            case 1:
                prompter.printMessage(false, "Choose A product management option:");
                processors.get(Modules.PRODUCTS).process();
                break;
            case 2:
                prompter.printMessage(false, "Choose A category management option:");
                processors.get(Modules.CATEGORIES).process();
                break;
            case 3:
                prompter.printMessage(false, "Choose A stock management option:");
                processors.get(Modules.STOCK).process();
                break;
            case 4:
                processors.get(Modules.DISCOUNTS).process();
                break;
            case 5:
                prompter.printMessage(false, "Choose A defects management option:");
                processors.get(Modules.DEFECTS).process();
                break;
            case 6:
                prompter.printMessage(false, "Choose A report to issue:");
                processors.get(Modules.REPORTS).process();
                return false;
            case 7:
                prompter.printMessage(false, null);
                activeStoreBranch = null;
                return false;
            case 8:
                prompter.printMessage(false, "Goodbye, hope you enjoyed our system.");
                shutdown = true;
                return false;

        }
        prompter.printMessage(false, null);
        return true;
    }
}
