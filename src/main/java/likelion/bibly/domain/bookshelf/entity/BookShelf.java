package likelion.bibly.domain.bookshelf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.group.entity.Group;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookshelf")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookShelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelf_id")
    private Long shelfId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "shelf_type")
    private ShelfType shelfType;

    @Builder
    public BookShelf(Group group, Book book, ShelfType shelfType) {
        this.group = group;
        this.book = book;
        this.shelfType = shelfType;
    }

    // shelfType 변경
    public void updateShelfType(ShelfType shelfType) {
        this.shelfType = shelfType;
    }
}