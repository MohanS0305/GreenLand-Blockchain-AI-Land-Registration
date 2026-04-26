// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract LandTransaction {
    struct Transaction {
        string buyerName;
        string buyerAddress;
        string sellerName;
        string sellerAddress;
        uint256 area;
        string city;
        string state;
        uint256 propertyId;
        uint256 physicalSurveyNo;
        uint256 price;               // Price in wei
        uint256 transactionDate;     // Unix timestamp
    }

    mapping(string => Transaction) public transactions;

    event TransactionCreated(
        string indexed transactionId,
        string buyerName,
        string buyerAddress,
        string sellerName,
        string sellerAddress,
        uint256 area,
        string city,
        string state,
        uint256 propertyId,
        uint256 physicalSurveyNo,
        uint256 price,
        uint256 transactionDate
    );

    event PaymentMade(
        address indexed buyer,
        address indexed seller,
        uint256 amount,
        string transactionId
    );

    event PaymentAttempt(
        address indexed buyer,
        address indexed seller,
        uint256 amount,
        string transactionId
    );

    event PaymentOutcome(
        bool success,
        string transactionId
    );

    function makeTransaction(
        string memory buyerName,
        string memory buyerAddress,
        string memory sellerName,
        string memory sellerAddress,
        uint256 area,
        string memory city,
        string memory state,
        uint256 propertyId,
        uint256 physicalSurveyNo,
        address payable seller,
        uint256 price
    ) public payable returns (string memory) {
        require(msg.value == price, "Transaction must include the exact price.");
        require(msg.sender != seller, "Buyer and seller cannot be the same.");
        
        string memory transactionId = string(abi.encodePacked(block.timestamp, "-", msg.sender, "-", seller, "-", propertyId));

        transactions[transactionId] = Transaction({
            buyerName: buyerName,
            buyerAddress: buyerAddress,
            sellerName: sellerName,
            sellerAddress: sellerAddress,
            area: area,
            city: city,
            state: state,
            propertyId: propertyId,
            physicalSurveyNo: physicalSurveyNo,
            price: price,
            transactionDate: block.timestamp
        });

        emit PaymentAttempt(msg.sender, seller, price, transactionId);
        (bool success, ) = seller.call{value: price}("");
        emit PaymentOutcome(success, transactionId);

        require(success, "Transaction failed. Transfer to seller unsuccessful.");

        emit TransactionCreated(transactionId, buyerName, buyerAddress, sellerName, sellerAddress, area, city, state, propertyId, physicalSurveyNo, price, block.timestamp);
        emit PaymentMade(msg.sender, seller, price, transactionId);

        return transactionId;
    }
}