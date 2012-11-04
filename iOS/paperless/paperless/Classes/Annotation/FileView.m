//
//  FileView.m
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import "FileView.h"
#import <QuartzCore/QuartzCore.h>

@interface FileView ()

@property (nonatomic, strong) UIScrollView *scrollView;
@property (nonatomic, strong) SmoothLineView *teacherView;
@property (nonatomic, strong) SmoothLineView *studentView;
@property (nonatomic, strong) UIToolbar *toolbar;
@property (nonatomic, strong) UIView *scrollingView;
@property (nonatomic, strong) UIImageView *teacherOverlay;
@property (nonatomic, strong) UIImageView *studentOverlay;

@end

@implementation FileView

@synthesize backgroundImageView = backgroundImageView_;
@synthesize thePenMode = thePenMode_;


+ (UIImage *) imageWithView:(UIView *)view
{
    UIGraphicsBeginImageContextWithOptions(view.bounds.size, view.opaque, 0.0);
    [view.layer renderInContext:UIGraphicsGetCurrentContext()];
    
    UIImage * img = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return img;
}

- (void)viewDidLoad
{
    self.toolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(0.0, 0.0, self.view.bounds.size.width, 44.0)];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    UIBarButtonItem *saveButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(saveTeacherOverlay)];
    
    UIBarButtonItem *deleteButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemTrash target:self action:@selector(deleteTeacherOverlay)];
    
    self.toolbar.items = @[flexibleSpace,saveButton, flexibleSpace,  deleteButton, flexibleSpace];
    [self.view addSubview:self.toolbar];
    
    self.backgroundImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ooo.png"]];
    
    self.studentOverlay = [[UIImageView alloc] initWithFrame:self.backgroundImageView.bounds];
    self.studentOverlay.opaque = NO;
    
    self.teacherOverlay = [[UIImageView alloc] initWithFrame:self.backgroundImageView.bounds];
    self.teacherOverlay.opaque = NO;

    self.studentView = [[SmoothLineView alloc] initWithFrame:self.backgroundImageView.bounds];
    self.studentView.opaque = NO;
    self.studentView.currentPenMode = StudentView;
    
    self.teacherView = [[SmoothLineView alloc] initWithFrame:self.backgroundImageView.bounds];
    self.teacherView.opaque = NO;
    self.teacherView.currentPenMode = TeacherView;
    
    
    self.scrollingView = [[UIView alloc] initWithFrame:self.backgroundImageView.bounds];
    [self.scrollingView addSubview:self.backgroundImageView];
    
    [self.scrollingView addSubview:self.studentOverlay];
    [self.scrollingView addSubview:self.teacherOverlay];
    
    [self.scrollingView addSubview:self.studentView];
    [self.scrollingView addSubview:self.teacherView];
    
    [self.scrollingView bringSubviewToFront:self.teacherView];
    
    
    CMStore *store = [CMStore defaultStore];
    
    [store fileWithName:@"student.png" additionalOptions:nil callback:^(CMFileFetchResponse *response)
    {
        if (response.file.fileData != nil)
        {
            NSData *imageData = response.file.fileData;
            UIImage *image = [UIImage imageWithData:imageData];
            self.studentOverlay.image = image;
        }
        else
        {
            self.studentOverlay.image = nil;
        }
    }];
    
    [store fileWithName:@"teacher.png" additionalOptions:nil callback:^(CMFileFetchResponse *response)
    {
        if (response.file.fileData != nil)
        {
            NSData *imageData = response.file.fileData;
            UIImage *image = [UIImage imageWithData:imageData];
            self.teacherOverlay.image = image;
        }
        else
        {
            self.teacherOverlay.image = nil;
        }

    }];

    self.scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0.0, 44.0, self.view.bounds.size.width, self.view.bounds.size.height)];
    self.scrollView.backgroundColor = [UIColor scrollViewTexturedBackgroundColor];
    self.scrollView.delegate = self;
    [self.scrollView setMinimumZoomScale:0.5];
    [self.scrollView setMaximumZoomScale:6.0];
    self.scrollView.clipsToBounds = YES;
    self.scrollView.contentSize = self.backgroundImageView.bounds.size;
    
    [self.scrollView addSubview:self.scrollingView];
    self.scrollView.scrollEnabled = NO;
    
    [self.view addSubview:self.scrollView];
}

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return self.scrollingView;
}

- (void)setThePenMode:(PenMode)thePenMode
{
    thePenMode_ = thePenMode;
    if (thePenMode == TeacherView)
    {
//        self.teacherView.hidden = NO;
        [self.scrollingView bringSubviewToFront:self.teacherView];
    }
    else
    {
//        self.studentView.hidden = NO;
        [self.scrollingView bringSubviewToFront:self.studentView];
    }
}

- (void)saveTeacherOverlay
{
    CMStore *store = [CMStore defaultStore];
    UIImage *image = [FileView imageWithView:self.teacherView];
    [store saveFileWithData:UIImagePNGRepresentation(image)
                      named:@"teacher.png"
          additionalOptions:nil
                   callback:^(CMFileUploadResponse *response) {
                       switch(response.result) {
                           case CMFileCreated:
                               NSLog(@"File Created. Go go go.");
                               break;
                           case CMFileUpdated:
                               // the file was updated, do something with it
                               NSLog(@"File Updated. Go go go.");
                               break;
                           case CMFileUploadFailed:
                               // upload failed!
                               NSLog(@"Rut roh rorge. Shit's been broken.");
                               break;
                       }
                   }
     ];
    [self reloadData];
}

- (void)deleteTeacherOverlay
{
    CMStore *store = [CMStore defaultStore];
    [store deleteFileNamed:@"teacher.png" additionalOptions:nil callback:^(CMDeleteResponse *response){
    
    }];
    self.teacherOverlay.image = nil;
    [self.teacherView removeFromSuperview];
    self.teacherView = [[SmoothLineView alloc] initWithFrame:self.backgroundImageView.bounds];
    self.teacherView.opaque = NO;
    self.teacherView.currentPenMode = TeacherView;
    [self.scrollingView addSubview:self.teacherView];

}

- (void)reloadData
{
    CMStore *store = [CMStore defaultStore];
    
    [store fileWithName:@"student.png" additionalOptions:nil callback:^(CMFileFetchResponse *response)
     {
         if (response.file.fileData != nil)
         {
             NSData *imageData = response.file.fileData;
             UIImage *image = [UIImage imageWithData:imageData];
             self.studentOverlay.image = image;
         }
         else
         {
             self.studentOverlay.image = nil;
         }
     }];
    
    [store fileWithName:@"teacher.png" additionalOptions:nil callback:^(CMFileFetchResponse *response)
     {
         if (response.file.fileData != nil)
         {
             NSData *imageData = response.file.fileData;
             UIImage *image = [UIImage imageWithData:imageData];
             self.teacherOverlay.image = image;
         }
         else
         {
             self.teacherOverlay.image = nil;
         }
         
     }];
}


@end
