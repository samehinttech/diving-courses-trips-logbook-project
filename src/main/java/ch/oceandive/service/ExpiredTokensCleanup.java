package ch.oceandive.service;

import ch.oceandive.repository.PremiumUserRepo;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ExpiredTokensCleanup {
  private static final Logger logger = LoggerFactory.getLogger(ExpiredTokensCleanup.class);

  private final PremiumUserRepo premiumUserRepo;

  public ExpiredTokensCleanup(PremiumUserRepo premiumUserRepo) {
    this.premiumUserRepo = premiumUserRepo;
  }
  /**
   * Scheduled task to clear expired password reset tokens.
   * Runs daily at midnight.
   * This method checks for password reset tokens that have expired and removes them from the database.
   * Maybe One day The Web App will be A live 😊😊😊
   */
  @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
  @Transactional
  public void cleanupExpiredPasswordResetTokens() {
    logger.info("Starting cleanup of expired password reset tokens");
    LocalDateTime now = LocalDateTime.now();
    int count = premiumUserRepo.clearExpiredPasswordResetTokens(now);
    logger.info("Cleared {} expired password reset tokens", count);
  }

}
