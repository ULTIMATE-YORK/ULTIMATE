package parameters;

import java.io.IOException;

public interface IParameter {

    public String getNameInModel();

    public String toString();

    public String getValue() throws IOException;


}
