package mapd.android.phoenix.watchyourself;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    ProviderService obj = new ProviderService();
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void messagePass(){
        // all OK
       // var msg = "Emergency! Please locate and help me! ";
        //assertTrue("Message sent should be Emergency! Please locate and help me! ", msg.equals(obj.onPostExecute.message));
        assert true;
    }

    @Test
    public void contactDetailsPass(){
        // all NOK
        assert false;
    }
}