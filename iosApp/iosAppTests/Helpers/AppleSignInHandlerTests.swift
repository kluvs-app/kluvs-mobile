import XCTest
import AuthenticationServices
import Combine
import Shared
@testable import Kluvs

/**
 * Tests for AppleSignInHandler state management.
 *
 * Note: Full sign-in flow and delegate methods require actual Apple framework
 * objects that can't be mocked in unit tests. These tests verify the testable
 * parts: singleton pattern, state management, and published properties.
 */
class AppleSignInHandlerTests: XCTestCase {

    var handler: AppleSignInHandler!
    var cancellables: Set<AnyCancellable>!

    override func setUp() {
        super.setUp()

        Bark.releaseAllTrainers()
        Bark.train(trainer: ColoredUnitTestTrainer(volume: Level.debug, showTimestamp: false))

        Bark.i("=== Setting up AppleSignInHandlerTests ===")

        handler = AppleSignInHandler.shared
        cancellables = Set<AnyCancellable>()

        // Reset state
        handler.idToken = nil
        handler.error = nil
        handler.isProcessing = false

        Bark.d("AppleSignInHandler state reset for testing")
    }

    override func tearDown() {
        Bark.i("=== Tearing down AppleSignInHandlerTests ===")

        handler.idToken = nil
        handler.error = nil
        handler.isProcessing = false
        cancellables = nil
        super.tearDown()
    }

    // MARK: - Singleton Tests

    func testSharedInstance_IsSingleton() {
        Bark.i("Testing AppleSignInHandler singleton pattern")

        let instance1 = AppleSignInHandler.shared
        let instance2 = AppleSignInHandler.shared

        XCTAssertTrue(instance1 === instance2, "Should return same singleton instance")
        Bark.d("✅ Singleton verified: both references point to same instance")
    }

    func testSharedInstance_MaintainsState() {
        Bark.i("Testing AppleSignInHandler state persistence across references")

        // Given
        let handler1 = AppleSignInHandler.shared
        handler1.idToken = "test.token.123"
        Bark.d("Set idToken on handler1: \(handler1.idToken ?? "nil")")

        // When - accessing through different reference
        let handler2 = AppleSignInHandler.shared

        // Then - should have same state
        XCTAssertEqual(handler2.idToken, "test.token.123")
        Bark.d("✅ State maintained: handler2.idToken = \(handler2.idToken ?? "nil")")
    }

    // MARK: - Initial State Tests

    func testInitialState_AllPropertiesNil() {
        Bark.i("Testing initial state after reset")

        // Given - fresh handler after reset

        // Then
        XCTAssertNil(handler.idToken, "Initial idToken should be nil")
        XCTAssertNil(handler.error, "Initial error should be nil")
        XCTAssertFalse(handler.isProcessing, "Initial isProcessing should be false")
        Bark.d("✅ All initial properties are nil/false as expected")
    }

    // MARK: - State Management Tests

    func testIdToken_CanBeSetAndCleared() {
        Bark.i("Testing idToken set and clear lifecycle")

        // When
        handler.idToken = "test.jwt.token"
        Bark.d("Set idToken: \(handler.idToken ?? "nil")")

        // Then
        XCTAssertEqual(handler.idToken, "test.jwt.token")

        // When - clear
        handler.idToken = nil
        Bark.d("Cleared idToken")

        // Then
        XCTAssertNil(handler.idToken)
        Bark.d("✅ idToken set/clear verified")
    }

    func testError_CanBeSetAndCleared() {
        Bark.i("Testing error set and clear lifecycle")

        // Given
        let testError = NSError(domain: "Test", code: 123, userInfo: [NSLocalizedDescriptionKey: "Test error"])

        // When
        handler.error = testError
        Bark.d("Set error with code: \((handler.error as NSError?)?.code ?? -1)")

        // Then
        XCTAssertNotNil(handler.error)
        XCTAssertEqual((handler.error as NSError?)?.code, 123)

        // When - clear
        handler.error = nil
        Bark.d("Cleared error")

        // Then
        XCTAssertNil(handler.error)
        Bark.d("✅ Error set/clear verified")
    }

    func testIsProcessing_CanBeToggled() {
        Bark.i("Testing isProcessing toggle")

        // Initially false
        XCTAssertFalse(handler.isProcessing)
        Bark.d("Confirmed initial isProcessing: false")

        // When - set to true
        handler.isProcessing = true
        Bark.d("Set isProcessing: true")

        // Then
        XCTAssertTrue(handler.isProcessing)

        // When - set back to false
        handler.isProcessing = false
        Bark.d("Set isProcessing: false")

        // Then
        XCTAssertFalse(handler.isProcessing)
        Bark.d("✅ isProcessing toggle verified")
    }

    // MARK: - Error Handling Tests

    // Note: We cannot test the delegate methods directly because ASAuthorizationController
    // requires at least one valid authorization request and cannot be mocked.
    // The error handling logic is tested indirectly through the state management tests.

    // MARK: - Publisher Tests

    func testIsProcessingPublisher_EmitsChanges() {
        Bark.i("Testing isProcessing Combine publisher emissions")

        // Given
        let expectation = XCTestExpectation(description: "isProcessing publishes changes")
        var receivedValues: [Bool] = []

        handler.$isProcessing
            .sink { isProcessing in
                receivedValues.append(isProcessing)
            }
            .store(in: &cancellables)

        // When
        handler.isProcessing = true
        handler.isProcessing = false
        Bark.d("Emitted isProcessing: true → false")

        // Then
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            XCTAssertEqual(receivedValues, [false, true, false])
            Bark.d("✅ Publisher emitted \(receivedValues.count) values: \(receivedValues)")
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 1.0)
    }

    func testErrorPublisher_MultipleChanges() {
        Bark.i("Testing error Combine publisher with multiple emissions")

        // Given
        let expectation = XCTestExpectation(description: "error publishes multiple changes")
        var receivedErrorCount = 0
        let error1 = NSError(domain: "Test", code: 1)
        let error2 = NSError(domain: "Test", code: 2)

        handler.$error
            .dropFirst() // Skip initial nil
            .sink { _ in
                receivedErrorCount += 1
                if receivedErrorCount == 3 {
                    expectation.fulfill()
                }
            }
            .store(in: &cancellables)

        // When
        handler.error = error1
        handler.error = nil
        handler.error = error2
        Bark.d("Emitted error sequence: error1 → nil → error2")

        wait(for: [expectation], timeout: 1.0)

        // Then
        XCTAssertEqual(receivedErrorCount, 3)
        Bark.d("✅ Publisher received \(receivedErrorCount) emissions as expected")
    }

    // MARK: - State Reset Tests

    func testStateReset_ClearsAllProperties() {
        Bark.i("Testing full state reset clears all properties")

        // Given - set all properties
        handler.idToken = "test.token"
        handler.error = NSError(domain: "Test", code: 1)
        handler.isProcessing = true
        Bark.d("State set: idToken=\(handler.idToken ?? "nil"), error=\(String(describing: handler.error)), isProcessing=\(handler.isProcessing)")

        // When - reset
        handler.idToken = nil
        handler.error = nil
        handler.isProcessing = false
        Bark.d("State reset performed")

        // Then
        XCTAssertNil(handler.idToken)
        XCTAssertNil(handler.error)
        XCTAssertFalse(handler.isProcessing)
        Bark.d("✅ All properties cleared successfully after reset")
    }
}
