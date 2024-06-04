package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.debt.DebtGroupDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.DebtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public DebtGroupDetailDto getById(String userEmail, Long groupId) throws NotFoundException {
        LOGGER.trace("getById({}, {})", userEmail, groupId);


        groupRepository.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found"));


        List<Object[]> rawResults = expenseRepository.calculateBalancesExpensesAndPaymentsForUser(userEmail, groupId);
        if (rawResults.isEmpty()) {
            return DebtGroupDetailDto.builder()
                .userEmail(userEmail)
                .groupId(groupId)
                .membersDebts(new HashMap<>())
                .build();
        }

        Map<String, Double> participantsMap = new HashMap<>();
        for (Object[] result : rawResults) {
            String participantEmail = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            participantsMap.put(participantEmail, amount.doubleValue());
        }

        return DebtGroupDetailDto.builder()
            .userEmail(userEmail)
            .groupId(groupId)
            .membersDebts(participantsMap)
            .build();

    }
}
