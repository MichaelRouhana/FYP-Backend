package com.example.AzureTestProject.Api.Service;

import com.example.AzureTestProject.Api.Entity.SequenceCounter;
import com.example.AzureTestProject.Api.Repository.SequenceCounterRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    private final SequenceCounterRepository repository;

    public SequenceGeneratorService(SequenceCounterRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public long getNextSequence() {
        int retryCount = 3;

        for (int i = 0; i < retryCount; i++) {
            try {
                SequenceCounter counter = repository.findById(1L)
                        .orElseGet(() -> new SequenceCounter(1L, 0L));

                long nextValue = counter.getLastValue() + 1;
                counter.setLastValue(nextValue);
                repository.save(counter);

                return nextValue;

            } catch (OptimisticLockException | org.springframework.dao.OptimisticLockingFailureException e) {
                if (i == retryCount - 1) {
                    throw e;
                }
            }
        }

        throw new IllegalStateException("Failed to generate sequence after retries");
    }
}
