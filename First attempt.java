import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Collectors;

class Scratch {
    static ArrayList readyPhotoSession = new ArrayList();
    static ArrayList<ArrayList<Integer>> groupFamily =  new ArrayList<ArrayList<Integer>>();

    static ArrayList arrangeKids(int[] kids, int startYoungestAt, int startEldestAt, int cousin) {
        // Stop if the eldest has covered all(reached end of the list)
        if (startEldestAt == kids.length)
            return readyPhotoSession;

            // Handle every kid and move to the start of line (Increment the end point and start from 0)
        else if (startYoungestAt > startEldestAt)
            arrangeKids(kids, 0, startEldestAt + 1, cousin);

            // Organize the group once you have a unique group(one possible way)
        else {
            ArrayList kidsGroup = new ArrayList();

            for (int i = startYoungestAt; i < startEldestAt; i++) {

                kidsGroup.add(kids[i]);
            }

            kidsGroup.add(kids[startEldestAt]);
            arrangeKids(kids, startYoungestAt + 1, startEldestAt, cousin);

            //Add the elder kid to every photo group - kid from other family
            kidsGroup.add(cousin);


            //Add the arrangement to the photo session(this array becomes part of the larger array)
            readyPhotoSession.add(kidsGroup);
        }

        //Once there are no other possible ways, return the complete groups of possible sessions
        return readyPhotoSession;
    }

    //Function to check if even
    static boolean IfEven(int x) {

        if (x % 2 == 0 && x != 0) {
            return true;
        } else {
            return false;
        }
    }

    //Function to check if Odd
    static boolean IfOdd(int y) {

        if (y % 2 == 1 && y != 0) {
            return true;
        } else {
            return false;
        }
    }

    static ArrayList<Integer> EvenNos(int rangeEven, int min) {
        ArrayList<Integer> EvenCombination = new ArrayList<>();


        for (int i = min; EvenCombination.size() < rangeEven; i++) {

            if (IfEven(i)) {
                EvenCombination.add(i);
            }
        }

        return EvenCombination;
    }

    //ODD NUMBERS WITHIN RANGE
    static ArrayList<Integer> OddNos(int rangeOdd, int min) {
        ArrayList<Integer> OddCombination = new ArrayList<>();

        //Measure by size of already harvested odd numbers.. > .size()
        for (int j = min; OddCombination.size() < rangeOdd; j++) {

            if (IfOdd(j)) {
                OddCombination.add(j);
            }
        }

        return OddCombination;


    }

    private static ArrayList<ArrayList<Integer>> preparePhotos(ArrayList<Integer> family, ArrayList<Integer> otherFamily){


        for (final int kidToTakePhoto : family){

            //split the even family kids to those younger than friend odd

            ArrayList younger = (ArrayList) otherFamily.stream().filter(k -> k < kidToTakePhoto).collect(Collectors.toList());

            int[] ret = new int[younger.size()];
            Iterator<Integer> iterator = younger.iterator();

            for (int i = 0; i < ret.length; i++) {
                ret[i] = iterator.next();
            }

            ArrayList combo = arrangeKids(ret, 0, 0, kidToTakePhoto);
            if(!groupFamily.contains(combo)){
                groupFamily.add(combo);
            }

        }

        return  groupFamily;
    }


    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Odd count: ");
        int oddCount = sc.nextInt();
        System.out.println("Enter Even count: ");
        int evenCount = sc.nextInt();
        sc.close();
        //Ensure to check the provided values whether they are within accepted range

        if(oddCount > 51){
            System.out.println("Provided odd count is beyond range");
            System.exit(0);
        }else if(evenCount < 1){
            System.out.println("Provided even count is too low");
            System.exit(0);
        }

        final ArrayList<Integer> odd = OddNos(oddCount, 0);
        ArrayList<Integer> even = EvenNos(evenCount, 0);

        //start with odd family:

        preparePhotos(odd, even);

        //then even family
        preparePhotos(even, odd);

        System.out.println("Possible photos combination: "+groupFamily.get(0).size());

        System.out.println(groupFamily.get(0));


        //System.out.println("ODD: " + odd.toString() + " Even: " + even.toString());

    }

}