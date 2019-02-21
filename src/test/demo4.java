/**
 * Created by tttppp606 on 2019/2/15.
 */
public class demo4 {
    public static void main(String[] args) {
        int i = 0;
        String string = new String("李闯");

        demo4 demo4 = new demo4();

        demo4.add(i);
        System.out.println(i);

        demo4.add(string);
        System.out.println(string);
    }
    private void add(int i){
        i = 2;
    }
    private void add(String str){
        str = "郭提";
    }
}
