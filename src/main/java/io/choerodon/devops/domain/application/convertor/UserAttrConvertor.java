package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
=======
import io.choerodon.devops.api.vo.UserAttrVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.infra.dataobject.UserAttrDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class UserAttrConvertor implements ConvertorI<UserAttrE, UserAttrDTO, UserAttrVO> {

    @Override
    public UserAttrE doToEntity(UserAttrDTO userAttrDO) {
        UserAttrE userAttrE = new UserAttrE();
        BeanUtils.copyProperties(userAttrDO, userAttrE);
        return userAttrE;
    }

    @Override
    public UserAttrDTO entityToDo(UserAttrE userAttrE) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        BeanUtils.copyProperties(userAttrE, userAttrDO);
        return userAttrDO;
    }

    @Override
    public UserAttrVO entityToDto(UserAttrE userAttrE) {
        UserAttrVO userAttrDTO = new UserAttrVO();
        BeanUtils.copyProperties(userAttrE, userAttrDTO);
        return userAttrDTO;
    }
}
