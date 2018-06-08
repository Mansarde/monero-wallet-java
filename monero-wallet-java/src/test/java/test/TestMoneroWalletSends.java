package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import api.MoneroAccount;
import api.MoneroAddress;
import api.MoneroPayment;
import api.MoneroTransaction;
import utils.TestUtils;

/**
 * Tests sending transactions within a Monero wallet.
 * 
 * These tests are separated since they rely on a balance and initiate transactions on the blockchain.
 * 
 * @author woodser
 */
public class TestMoneroWalletSends {
  
  private static final Integer MIXIN = 6;
  private static final int UNLOCKED_DIVISOR = 20;
  
  private MoneroAccount wallet;

  @Before
  public void setup() throws Exception {
    wallet = TestUtils.getWallet();
  }

  @Test
  public void testSendPayment() {
    
    // get balance before
    BigInteger balanceBefore = wallet.getBalance();
    BigInteger unlockedBalanceBefore = wallet.getUnlockedBalance();
    
    // send to self
    MoneroAddress address = wallet.getStandardAddress();
    BigInteger sendAmount = unlockedBalanceBefore.divide(BigInteger.valueOf(UNLOCKED_DIVISOR));
    MoneroTransaction tx = wallet.send(address.toString(), sendAmount, null, MIXIN, 0);
    
    // test transaction
    assertNotNull(tx.getPayments());
    assertEquals(1, tx.getPayments().size());
    assertTrue(tx.getFee().longValue() > 0);
    assertEquals(MIXIN, tx.getMixin());
    assertNotNull(tx.getKey());
    assertNotNull(tx.getHash());
    assertNull(tx.getSize());
    assertNull(tx.getType());
    assertNull(tx.getHeight());
    assertEquals((Integer) 0, tx.getUnlockTime());
    
    // test payments
    for (MoneroPayment payment : tx.getPayments()) {
      assertEquals(address.toString(), payment.getAddress());
      assertEquals(sendAmount, payment.getAmount());
      assertTrue(tx == payment.getTransaction());
    }
    
    // test wallet balance
    assertTrue(wallet.getBalance().longValue() < balanceBefore.longValue());
    assertTrue(wallet.getUnlockedBalance().longValue() < unlockedBalanceBefore.longValue());
  }
  
  @Test
  public void testSendPayments() {
    
    // get balance and address
    BigInteger balanceBefore = wallet.getBalance();
    BigInteger unlockedBalanceBefore = wallet.getUnlockedBalance();
    MoneroAddress address = wallet.getStandardAddress();
    
    // create payments to send
    int numPayments = 3;
    BigInteger sendAmount = unlockedBalanceBefore.divide(BigInteger.valueOf(numPayments + UNLOCKED_DIVISOR));
    List<MoneroPayment> payments = new ArrayList<MoneroPayment>();
    for (int i = 0; i < numPayments; i++) {
      payments.add(new MoneroPayment(address.toString(), sendAmount));
    }
    
    // send payments
    MoneroTransaction tx = wallet.send(payments, null, MIXIN, 0);
    
    // test transaction
    assertNotNull(tx.getPayments());
    assertEquals(numPayments, tx.getPayments().size());
    assertTrue(tx.getFee().longValue() > 0);
    assertEquals(MIXIN, tx.getMixin());
    assertNotNull(tx.getKey());
    assertNotNull(tx.getHash());
    assertNull(tx.getSize());
    assertNull(tx.getType());
    assertNull(tx.getHeight());
    assertEquals((Integer) 0, tx.getUnlockTime());
    
    // test payments
    for (MoneroPayment payment : tx.getPayments()) {
      assertEquals(address.toString(), payment.getAddress());
      assertEquals(sendAmount, payment.getAmount());
      assertTrue(tx == payment.getTransaction());
    }
    
    // test wallet balance
    assertTrue(wallet.getBalance().longValue() < balanceBefore.longValue());
    assertTrue(wallet.getUnlockedBalance().longValue() < unlockedBalanceBefore.longValue());
  }
  
  @Test
  public void testSendSplit() {
    
    // get balance and address
    BigInteger balanceBefore = wallet.getBalance();
    BigInteger unlockedBalanceBefore = wallet.getUnlockedBalance();
    MoneroAddress address = wallet.getStandardAddress();
    
    // create payments to send
    int numPayments = 3;
    BigInteger sendAmount = unlockedBalanceBefore.divide(BigInteger.valueOf(numPayments + UNLOCKED_DIVISOR));
    List<MoneroPayment> payments = new ArrayList<MoneroPayment>();
    for (int i = 0; i < numPayments; i++) {
      payments.add(new MoneroPayment(address.toString(), sendAmount));
    }
    
    // send payments
    List<MoneroTransaction> txs = wallet.sendSplit(payments, null, MIXIN, 0, true);
    
    // test transactions
    for (MoneroTransaction tx : txs) {
      assertNull(tx.getPayments());
      assertTrue(tx.getFee().longValue() > 0);
      assertEquals(MIXIN, tx.getMixin());
      assertNull(tx.getKey());
      assertNotNull(tx.getHash());
      assertNull(tx.getHeight());
      assertNull(tx.getSize());
      assertNull(tx.getType());
      assertNull(tx.getHeight());
      assertEquals((Integer) 0, tx.getUnlockTime());
    }
    
    // test wallet balance
    assertTrue(wallet.getBalance().longValue() < balanceBefore.longValue());
    assertTrue(wallet.getUnlockedBalance().longValue() < unlockedBalanceBefore.longValue());
  }

  @Test
  public void testSweepDust() {
    List<MoneroTransaction> txs = wallet.sweepDust();
    for (MoneroTransaction tx : txs) {
      assertNull(tx.getPayments());
      assertNull(tx.getFee());
      assertNull(tx.getMixin());
      assertNull(tx.getKey());
      assertNotNull(tx.getHash());
      assertNull(tx.getSize());
      assertNull(tx.getType());
      assertNull(tx.getHeight());
    }
  }
}
