package io.choerodon.devops.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] arg0)
    {
        List<String> resultes  = new ArrayList();
        resultes.add("a");
        resultes.add("b");
        resultes.add("c");
        List<String> haha = resultes.stream().map(s->
        {return s+s;}).collect(Collectors.toList());
        System.out.print(haha);
    }
}
